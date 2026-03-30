package com.jemini.stayhost.channel.application.service;

import com.jemini.stayhost.channel.domain.component.ChannelAdapter;
import com.jemini.stayhost.channel.domain.component.ChannelMappingReader;
import com.jemini.stayhost.channel.domain.component.ChannelRoomMappingReader;
import com.jemini.stayhost.channel.domain.component.ChannelSyncLogManager;
import com.jemini.stayhost.channel.domain.dto.ChannelSyncResult;
import com.jemini.stayhost.channel.domain.dto.InventoryUpdate;
import com.jemini.stayhost.channel.domain.model.ChannelPropertyMapping;
import com.jemini.stayhost.channel.domain.model.ChannelRoomMapping;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.model.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChannelManagerServiceTest {

    private ChannelManagerService channelManagerService;

    @Mock
    private ChannelMappingReader channelMappingReader;

    @Mock
    private ChannelRoomMappingReader channelRoomMappingReader;

    @Mock
    private ChannelSyncLogManager channelSyncLogManager;

    @Mock
    private InventoryReader inventoryReader;

    @Mock
    private ChannelAdapter mockAdapter;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private static final Long PROPERTY_ID = 1L;
    private static final Long ROOM_TYPE_ID = 10L;
    private static final Long CHANNEL_ID = 100L;
    private static final Long MAPPING_ID = 200L;

    @BeforeEach
    void setUp() {
        channelManagerService = new ChannelManagerService(
            channelMappingReader, channelRoomMappingReader, channelSyncLogManager,
            inventoryReader, List.of(mockAdapter), executor
        );
    }

    @Test
    @DisplayName("매핑된 채널이 없으면 푸시하지 않는다")
    void 매핑된_채널이_없으면_푸시하지_않는다() {
        given(channelMappingReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of());

        channelManagerService.pushInventoryToChannels(PROPERTY_ID, ROOM_TYPE_ID, List.of(LocalDate.of(2026, 4, 10)));

        verify(mockAdapter, never()).pushInventory(any(), any());
    }

    @Test
    @DisplayName("매핑된 채널에 재고를 병렬 푸시한다")
    void 매핑된_채널에_재고를_병렬_푸시한다() {
        final ChannelPropertyMapping mapping = createMapping();
        final ChannelRoomMapping roomMapping = ChannelRoomMapping.create(MAPPING_ID, ROOM_TYPE_ID, "EXT-ROOM-001");
        final List<LocalDate> dates = List.of(LocalDate.of(2026, 4, 10));

        given(channelMappingReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(mapping));
        given(channelRoomMappingReader.findByChannelPropertyMappingId(MAPPING_ID)).willReturn(List.of(roomMapping));
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), 5)));
        given(mockAdapter.pushInventory(any(), any())).willReturn(ChannelSyncResult.success("MOCK"));

        channelManagerService.pushInventoryToChannels(PROPERTY_ID, ROOM_TYPE_ID, dates);

        verify(mockAdapter).pushInventory(eq(mapping), any());
    }

    @Test
    @DisplayName("채널 푸시 실패해도 예외가 전파되지 않는다")
    void 채널_푸시_실패해도_예외가_전파되지_않는다() {
        final ChannelPropertyMapping mapping = createMapping();

        given(channelMappingReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(mapping));
        given(mockAdapter.pushInventory(any(), any())).willThrow(new RuntimeException("채널 타임아웃"));

        assertThatCode(() -> channelManagerService.pushInventoryToChannels(
            PROPERTY_ID, ROOM_TYPE_ID, List.of(LocalDate.of(2026, 4, 10))))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("룸 매핑이 없으면 UNMAPPED 키로 푸시한다")
    void 룸_매핑이_없으면_UNMAPPED_키로_푸시한다() {
        final ChannelPropertyMapping mapping = createMapping();
        final List<LocalDate> dates = List.of(LocalDate.of(2026, 4, 10));

        given(channelMappingReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(mapping));
        given(channelRoomMappingReader.findByChannelPropertyMappingId(MAPPING_ID)).willReturn(List.of());
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), 5)));
        given(mockAdapter.pushInventory(any(), any())).willReturn(ChannelSyncResult.success("MOCK"));

        channelManagerService.pushInventoryToChannels(PROPERTY_ID, ROOM_TYPE_ID, dates);

        verify(mockAdapter).pushInventory(eq(mapping), any());
    }

    @Test
    @DisplayName("여러 채널 매핑 중 일부 푸시가 실패해도 나머지는 정상 처리된다 (부분 실패)")
    void 여러_채널_매핑_중_일부_푸시가_실패해도_나머지는_정상_처리된다() {
        final ChannelPropertyMapping mapping1 = createMapping(MAPPING_ID, "EXT-PROP-001");
        final ChannelPropertyMapping mapping2 = createMapping(201L, "EXT-PROP-002");

        given(channelMappingReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(mapping1, mapping2));
        given(channelRoomMappingReader.findByChannelPropertyMappingId(any())).willReturn(List.of());
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), 5)));
        given(mockAdapter.pushInventory(eq(mapping1), any())).willReturn(ChannelSyncResult.success("MOCK"));
        given(mockAdapter.pushInventory(eq(mapping2), any())).willThrow(new RuntimeException("타임아웃"));

        assertThatCode(() -> channelManagerService.pushInventoryToChannels(
            PROPERTY_ID, ROOM_TYPE_ID, List.of(LocalDate.of(2026, 4, 10))))
            .doesNotThrowAnyException();

        verify(mockAdapter, times(2)).pushInventory(any(), any());
    }

    @Test
    @DisplayName("여러 날짜에 대한 재고를 한 번에 푸시한다")
    void 여러_날짜에_대한_재고를_한_번에_푸시한다() {
        final ChannelPropertyMapping mapping = createMapping(MAPPING_ID, "EXT-PROP-001");
        final ChannelRoomMapping roomMapping = ChannelRoomMapping.create(MAPPING_ID, ROOM_TYPE_ID, "EXT-ROOM-001");
        final List<LocalDate> dates = List.of(
            LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 11), LocalDate.of(2026, 4, 12));

        given(channelMappingReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(mapping));
        given(channelRoomMappingReader.findByChannelPropertyMappingId(MAPPING_ID)).willReturn(List.of(roomMapping));
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any()))
            .willReturn(List.of(
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 10), 5),
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 11), 3),
                Inventory.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 12), 0)));
        given(mockAdapter.pushInventory(any(), any())).willReturn(ChannelSyncResult.success("MOCK"));

        channelManagerService.pushInventoryToChannels(PROPERTY_ID, ROOM_TYPE_ID, dates);

        verify(mockAdapter).pushInventory(eq(mapping), any());
    }

    @Test
    @DisplayName("어댑터가 빈 리스트일 때 예외가 전파되지 않는다")
    void 어댑터가_빈_리스트일_때_예외가_전파되지_않는다() {
        final ChannelManagerService emptyAdapterService = new ChannelManagerService(
            channelMappingReader, channelRoomMappingReader, channelSyncLogManager,
            inventoryReader, List.of(), executor
        );
        final ChannelPropertyMapping mapping = createMapping(MAPPING_ID, "EXT-PROP-001");

        given(channelMappingReader.findActiveByPropertyId(PROPERTY_ID)).willReturn(List.of(mapping));

        assertThatCode(() -> emptyAdapterService.pushInventoryToChannels(
            PROPERTY_ID, ROOM_TYPE_ID, List.of(LocalDate.of(2026, 4, 10))))
            .doesNotThrowAnyException();
    }

    private ChannelPropertyMapping createMapping() {
        return createMapping(MAPPING_ID, "EXT-PROP-001");
    }

    private ChannelPropertyMapping createMapping(final Long id, final String externalPropertyId) {
        final ChannelPropertyMapping mapping = ChannelPropertyMapping.create(CHANNEL_ID, PROPERTY_ID, externalPropertyId);
        ReflectionTestUtils.setField(mapping, "id", id);
        return mapping;
    }
}
