package com.jemini.stayhost.booking.application.listener;

import com.jemini.stayhost.booking.infrastructure.cache.InventoryCache;
import com.jemini.stayhost.property.domain.event.InventoryChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryCacheSyncListener {

    private final InventoryCache inventoryCache;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInventoryChanged(final InventoryChangedEvent event) {
        inventoryCache.syncFromDb(event.roomTypeId(), event.affectedDates());
        log.info("InventoryCache 동기화: roomTypeId={}, dates={}", event.roomTypeId(), event.affectedDates().size());
    }
}
