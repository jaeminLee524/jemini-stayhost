package com.jemini.stayhost.booking.infrastructure.cache;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.domain.event.InventoryChangedEvent;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.infrastructure.persistence.InventoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryCache {

    private final InventoryRepository inventoryRepository;
    private final ConcurrentHashMap<String, AtomicInteger> cache = new ConcurrentHashMap<>();

    @PostConstruct
    void warmUp() {
        final LocalDate today = LocalDate.now();
        final LocalDate endDate = today.plusDays(90);
        final List<Inventory> inventories = inventoryRepository.findByDateBetween(today, endDate);

        inventories.forEach(inv -> {
            final String key = buildKey(inv.getRoomTypeId(), inv.getDate());
            cache.put(key, new AtomicInteger(inv.getAvailableCount()));
        });

        log.info("InventoryCache 워밍업 완료: {}건", inventories.size());
    }

    /**
     * CAS 1차 필터링. 모든 날짜의 재고를 원자적으로 감소시킨다. 하나라도 실패하면 이미 감소시킨 것을 복원하고 예외를 던진다.
     */
    public void tryDecreaseAll(final Long roomTypeId, final List<LocalDate> dates) {
        int decreasedCount = 0;

        for (final LocalDate date : dates) {
            final String key = buildKey(roomTypeId, date);
            final AtomicInteger available = cache.get(key);

            // available == null: 캐시에 키가 없는 경우 (워밍업 범위 밖, 신규 Inventory 등)
            // DB에는 재고가 존재할 수 있으나, 현재는 보수적으로 차단한다.
            // 판매 기회 손실을 허용하지 않으려면 null일 때 캐시를 통과시키고 DB 락에서 판단하도록 변경 가능.
            if (available == null || !tryDecrease(available)) {
                rollbackDecrease(roomTypeId, dates, decreasedCount);
                throw new BusinessException(ErrorCode.INVENTORY_INSUFFICIENT);
            }
            decreasedCount++;
        }
    }

    /**
     * 예약 취소 시 캐시 재고를 복원한다.
     */
    public void restore(final Long roomTypeId, final List<LocalDate> dates) {
        for (final LocalDate date : dates) {
            final String key = buildKey(roomTypeId, date);
            final AtomicInteger available = cache.get(key);
            if (available != null) {
                available.incrementAndGet();
            }
        }
    }

    /**
     * DB 비관적 락 실패 시 CAS에서 감소한 캐시를 롤백한다.
     */
    public void rollbackAll(final Long roomTypeId, final List<LocalDate> dates) {
        restore(roomTypeId, dates);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInventoryChanged(final InventoryChangedEvent event) {
        final List<Inventory> inventories = inventoryRepository.findByRoomTypeIdAndDateBetween(
            event.roomTypeId(), event.affectedDates().getFirst(), event.affectedDates().getLast());

        inventories.forEach(inv -> {
            final String key = buildKey(inv.getRoomTypeId(), inv.getDate());
            cache.put(key, new AtomicInteger(inv.getAvailableCount()));
        });

        log.debug("InventoryCache 동기화: roomTypeId={}, dates={}", event.roomTypeId(), event.affectedDates().size());
    }

    private boolean tryDecrease(final AtomicInteger available) {
        while (true) {
            final int current = available.get();
            if (current <= 0) {
                return false;
            }
            if (available.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }

    private void rollbackDecrease(
        final Long roomTypeId,
        final List<LocalDate> dates,
        final int count
    ) {
        for (int i = 0; i < count; i++) {
            final String key = buildKey(roomTypeId, dates.get(i));
            final AtomicInteger available = cache.get(key);
            if (available != null) {
                available.incrementAndGet();
            }
        }
    }

    private String buildKey(final Long roomTypeId, final LocalDate date) {
        return roomTypeId + ":" + date;
    }
}
