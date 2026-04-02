package com.jemini.stayhost.channel.application.service;

import com.jemini.stayhost.channel.domain.component.ChannelAdapter;
import com.jemini.stayhost.channel.domain.component.ChannelMappingReader;
import com.jemini.stayhost.channel.domain.component.ChannelReader;
import com.jemini.stayhost.channel.domain.component.ChannelRoomMappingReader;
import com.jemini.stayhost.channel.domain.component.ChannelSyncLogManager;
import com.jemini.stayhost.channel.domain.dto.ChannelSyncResult;
import com.jemini.stayhost.channel.domain.dto.InventoryUpdate;
import com.jemini.stayhost.channel.domain.model.Channel;
import com.jemini.stayhost.channel.domain.model.ChannelPropertyMapping;
import com.jemini.stayhost.channel.domain.model.ChannelRoomMapping;
import com.jemini.stayhost.channel.domain.model.ChannelSyncLog;
import com.jemini.stayhost.channel.domain.model.ChannelSyncType;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
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

    private static final String UNMAPPED_ROOM_PREFIX = "UNMAPPED-";

    private final ChannelMappingReader channelMappingReader;
    private final ChannelReader channelReader;
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

        final List<ChannelSyncResult> results = pushToAllChannels(mappings, roomTypeId, affectedDates);
        results.forEach(result -> saveSyncLog(result, propertyId));
    }

    /** 모든 매핑 채널에 비동기 병렬 푸시 후 결과를 수집한다. */
    private List<ChannelSyncResult> pushToAllChannels(
        final List<ChannelPropertyMapping> mappings,
        final Long roomTypeId,
        final List<LocalDate> affectedDates
    ) {
        final List<CompletableFuture<ChannelSyncResult>> futures = mappings.stream()
            .map(mapping -> CompletableFuture.supplyAsync(
                () -> pushInventory(mapping, roomTypeId, affectedDates), channelExecutor))
            .toList();

        return futures.stream()
            .map(CompletableFuture::join)
            .toList();
    }

    /** 단일 채널에 재고를 푸시한다. 실패 시 ChannelSyncResult.failure를 반환한다. */
    private ChannelSyncResult pushInventory(
        final ChannelPropertyMapping mapping,
        final Long roomTypeId,
        final List<LocalDate> affectedDates
    ) {
        try {
            final ChannelAdapter adapter = findAdapter(mapping.getChannelId());
            final List<InventoryUpdate> updates = buildInventoryUpdates(mapping, roomTypeId, affectedDates);
            return adapter.pushInventory(mapping, updates);
        } catch (final Exception e) {
            log.warn("채널 재고 푸시 실패: channelId={}, propertyId={}", mapping.getChannelId(), mapping.getPropertyId(), e);
            return ChannelSyncResult.failure("UNKNOWN", e.getMessage(), 0);
        }
    }

    /** 채널 ID로 등록된 ChannelAdapter 구현체를 찾는다. */
    private ChannelAdapter findAdapter(final Long channelId) {
        final Channel channel = channelReader.getById(channelId);

        return adapters.stream()
            .filter(adapter -> adapter.getChannelCode().equals(channel.getCode()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_ADAPTER_NOT_FOUND));
    }

    /** 채널에 전송할 InventoryUpdate 목록을 구성한다. */
    private List<InventoryUpdate> buildInventoryUpdates(
        final ChannelPropertyMapping mapping,
        final Long roomTypeId,
        final List<LocalDate> dates
    ) {
        final String externalRoomId = resolveExternalRoomId(mapping.getId(), roomTypeId);
        final List<Inventory> inventories = inventoryReader.findByRoomTypeIdAndDateBetween(roomTypeId, dates.getFirst(), dates.getLast());

        return inventories.stream()
            .map(inv -> new InventoryUpdate(externalRoomId, inv.getDate(), inv.getAvailableCount()))
            .toList();
    }

    /** 내부 roomTypeId를 채널 측 외부 객실 ID로 변환한다. 매핑이 없으면 UNMAPPED- 접두사를 붙인다. */
    private String resolveExternalRoomId(final Long channelPropertyMappingId, final Long roomTypeId) {
        final List<ChannelRoomMapping> roomMappings = channelRoomMappingReader.findByChannelPropertyMappingId(channelPropertyMappingId);

        return roomMappings.stream()
            .filter(rm -> rm.getRoomTypeId().equals(roomTypeId))
            .map(ChannelRoomMapping::getExternalRoomId)
            .findFirst()
            .orElse(UNMAPPED_ROOM_PREFIX + roomTypeId);
    }

    /** 동기화 결과를 로그로 기록하고 ChannelSyncLog를 영속화한다. */
    private void saveSyncLog(final ChannelSyncResult result, final Long propertyId) {
        log.info("채널 동기화 결과: channel={}, success={}, error={}", result.channelCode(), result.success(), result.errorMessage());

        final ChannelSyncLog syncLog = ChannelSyncLog.createOutbound(null, propertyId, ChannelSyncType.INVENTORY);
        if (result.isFailure()) {
            syncLog.markFailed(result.errorMessage());
        }
        channelSyncLogManager.save(syncLog);
    }
}
