package com.jemini.stayhost.channel.application.service;

import com.jemini.stayhost.channel.domain.component.ChannelAdapter;
import com.jemini.stayhost.channel.domain.component.ChannelMappingReader;
import com.jemini.stayhost.channel.domain.component.ChannelRoomMappingReader;
import com.jemini.stayhost.channel.domain.component.ChannelSyncLogManager;
import com.jemini.stayhost.channel.domain.dto.ChannelSyncResult;
import com.jemini.stayhost.channel.domain.dto.InventoryUpdate;
import com.jemini.stayhost.channel.domain.model.ChannelPropertyMapping;
import com.jemini.stayhost.channel.domain.model.ChannelRoomMapping;
import com.jemini.stayhost.channel.domain.model.ChannelSyncLog;
import com.jemini.stayhost.channel.domain.model.ChannelSyncType;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.model.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelManagerService {

    private final ChannelMappingReader channelMappingReader;
    private final ChannelRoomMappingReader channelRoomMappingReader;
    private final ChannelSyncLogManager channelSyncLogManager;
    private final InventoryReader inventoryReader;
    private final List<ChannelAdapter> adapters;
    private final Executor channelExecutor;

    /**
     * 재고 변경 시 매핑된 모든 채널에 CompletableFuture로 병렬 푸시한다.
     * 부분 실패를 허용하며, 실패한 채널만 로그에 기록한다.
     */
    public void pushInventoryToChannels(final Long propertyId, final Long roomTypeId, final List<LocalDate> affectedDates) {
        final List<ChannelPropertyMapping> mappings = channelMappingReader.findActiveByPropertyId(propertyId);
        if (mappings.isEmpty()) {
            return;
        }

        final List<CompletableFuture<ChannelSyncResult>> futures = mappings.stream()
            .map(mapping -> CompletableFuture.supplyAsync(
                () -> pushInventory(mapping, roomTypeId, affectedDates), channelExecutor))
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        futures.stream()
            .map(CompletableFuture::join)
            .forEach(result -> logSyncResult(result, propertyId));
    }

    private ChannelSyncResult pushInventory(
        final ChannelPropertyMapping mapping,
        final Long roomTypeId,
        final List<LocalDate> affectedDates
    ) {
        try {
            final ChannelAdapter adapter = findAdapter(mapping);
            final List<InventoryUpdate> updates = buildInventoryUpdates(mapping.getId(), roomTypeId, affectedDates);
            return adapter.pushInventory(mapping, updates);
        } catch (final Exception e) {
            log.warn("채널 재고 푸시 실패: channelId={}, propertyId={}", mapping.getChannelId(), mapping.getPropertyId(), e);
            return ChannelSyncResult.failure("UNKNOWN", e.getMessage(), 0);
        }
    }

    private ChannelAdapter findAdapter(final ChannelPropertyMapping mapping) {
        return adapters.stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("등록된 ChannelAdapter가 없습니다."));
    }

    private List<InventoryUpdate> buildInventoryUpdates(
        final Long channelPropertyMappingId,
        final Long roomTypeId,
        final List<LocalDate> dates
    ) {
        final List<ChannelRoomMapping> roomMappings = channelRoomMappingReader.findByChannelPropertyMappingId(channelPropertyMappingId);
        final String externalRoomId = roomMappings.stream()
            .filter(rm -> rm.getRoomTypeId().equals(roomTypeId))
            .map(ChannelRoomMapping::getExternalRoomId)
            .findFirst()
            .orElse("UNMAPPED-" + roomTypeId);

        final List<Inventory> inventories = inventoryReader.findByRoomTypeIdAndDateBetween(roomTypeId, dates.getFirst(), dates.getLast());

        return inventories.stream()
            .map(inv -> new InventoryUpdate(externalRoomId, inv.getDate(), inv.getAvailableCount()))
            .toList();
    }

    private void logSyncResult(final ChannelSyncResult result, final Long propertyId) {
        log.info("채널 동기화 결과: channel={}, success={}, error={}", result.channelCode(), result.success(), result.errorMessage());
    }
}
