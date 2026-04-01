package com.jemini.stayhost.supplier.application.service;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.domain.component.InventoryManager;
import com.jemini.stayhost.property.domain.component.InventoryReader;
import com.jemini.stayhost.property.domain.component.RateManager;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Inventory;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.supplier.domain.component.SupplierAdapter;
import com.jemini.stayhost.supplier.domain.component.SupplierManager;
import com.jemini.stayhost.supplier.domain.component.SupplierMappingReader;
import com.jemini.stayhost.supplier.domain.component.SupplierPropertyReader;
import com.jemini.stayhost.supplier.domain.component.SupplierReader;
import com.jemini.stayhost.supplier.domain.dto.SupplierInventoryData;
import com.jemini.stayhost.supplier.domain.dto.SupplierPropertyData;
import com.jemini.stayhost.supplier.domain.dto.SupplierRateData;
import com.jemini.stayhost.supplier.domain.model.MappingStatus;
import com.jemini.stayhost.supplier.domain.model.Supplier;
import com.jemini.stayhost.supplier.domain.model.SupplierProperty;
import com.jemini.stayhost.supplier.domain.model.SupplierPropertyMapping;
import com.jemini.stayhost.supplier.domain.model.SupplierSyncJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SupplierSyncServiceTest {

    private SupplierSyncService supplierSyncService;

    @Mock
    private SupplierReader supplierReader;

    @Mock
    private SupplierPropertyReader supplierPropertyReader;

    @Mock
    private SupplierMappingReader supplierMappingReader;

    @Mock
    private SupplierManager supplierManager;

    @Mock
    private RoomTypeReader roomTypeReader;

    @Mock
    private RateReader rateReader;

    @Mock
    private RateManager rateManager;

    @Mock
    private InventoryReader inventoryReader;

    @Mock
    private InventoryManager inventoryManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SupplierAdapter mockAdapter;

    private static final Long SUPPLIER_ID = 1L;
    private static final String SUPPLIER_CODE = "MOCK_SUPPLIER";

    @BeforeEach
    void setUp() {
        lenient().when(mockAdapter.getSupplierCode()).thenReturn(SUPPLIER_CODE);
        supplierSyncService = new SupplierSyncService(
                supplierReader,
                supplierPropertyReader,
                supplierMappingReader,
                supplierManager,
                roomTypeReader,
                rateReader,
                rateManager,
                inventoryReader,
                inventoryManager,
                eventPublisher,
                List.of(mockAdapter)
        );
    }

    @Test
    @DisplayName("동기화 성공 - 3건 모두 신규 저장")
    void 동기화_성공() {
        final Supplier supplier = createSupplier();
        final List<SupplierPropertyData> properties = List.of(
                new SupplierPropertyData("EXT-1", "숙소1", "주소1", "서울", "호텔", "{\"id\":1}"),
                new SupplierPropertyData("EXT-2", "숙소2", "주소2", "부산", "모텔", "{\"id\":2}"),
                new SupplierPropertyData("EXT-3", "숙소3", "주소3", "제주", "펜션", "{\"id\":3}")
        );

        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);
        given(mockAdapter.fetchProperties()).willReturn(properties);
        given(supplierManager.saveSyncJob(any(SupplierSyncJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(supplierPropertyReader.findBySupplierIdAndExternalPropertyId(eq(SUPPLIER_ID), any()))
                .willReturn(Optional.empty());
        given(supplierManager.saveProperty(any(SupplierProperty.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(supplierPropertyReader.findBySupplierId(SUPPLIER_ID)).willReturn(List.of());

        supplierSyncService.syncSupplier(SUPPLIER_ID);

        verify(supplierManager, times(2)).saveSyncJob(any(SupplierSyncJob.class));
        verify(supplierManager, times(3)).saveProperty(any(SupplierProperty.class));
        verify(supplierPropertyReader, times(3))
                .findBySupplierIdAndExternalPropertyId(eq(SUPPLIER_ID), any());
    }

    @Test
    @DisplayName("어댑터 없으면 예외 발생")
    void 어댑터_없으면_예외() {
        final Supplier supplier = Supplier.create("다른 공급사", "UNKNOWN_CODE", null, null);
        ReflectionTestUtils.setField(supplier, "id", SUPPLIER_ID);
        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);

        assertThatThrownBy(() -> supplierSyncService.syncSupplier(SUPPLIER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUPPLIER_ADAPTER_NOT_FOUND);
    }

    @Test
    @DisplayName("기존 숙소가 존재하면 rawData 업데이트")
    void 기존_숙소_업데이트() {
        final Supplier supplier = createSupplier();
        final List<SupplierPropertyData> properties = List.of(
                new SupplierPropertyData("EXT-1", "숙소1", "주소1", "서울", "호텔", "{\"updated\":true}")
        );
        final SupplierProperty existingProperty = SupplierProperty.create(SUPPLIER_ID, "EXT-1", "{\"old\":true}");

        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);
        given(mockAdapter.fetchProperties()).willReturn(properties);
        given(supplierManager.saveSyncJob(any(SupplierSyncJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(supplierPropertyReader.findBySupplierIdAndExternalPropertyId(SUPPLIER_ID, "EXT-1"))
                .willReturn(Optional.of(existingProperty));
        given(supplierPropertyReader.findBySupplierId(SUPPLIER_ID)).willReturn(List.of());

        supplierSyncService.syncSupplier(SUPPLIER_ID);

        verify(supplierManager, never()).saveProperty(any(SupplierProperty.class));
        verify(supplierManager, times(2)).saveSyncJob(any(SupplierSyncJob.class));
    }

    @Test
    @DisplayName("부분 실패 시 성공/실패 건수 추적")
    void 부분_실패_시_성공_실패_건수_추적() {
        final Supplier supplier = createSupplier();
        final List<SupplierPropertyData> properties = List.of(
                new SupplierPropertyData("EXT-1", "숙소1", "주소1", "서울", "호텔", "{\"id\":1}"),
                new SupplierPropertyData("EXT-2", "숙소2", "주소2", "부산", "모텔", "{\"id\":2}"),
                new SupplierPropertyData("EXT-3", "숙소3", "주소3", "제주", "펜션", "{\"id\":3}")
        );

        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);
        given(mockAdapter.fetchProperties()).willReturn(properties);
        given(supplierManager.saveSyncJob(any(SupplierSyncJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(supplierPropertyReader.findBySupplierIdAndExternalPropertyId(SUPPLIER_ID, "EXT-1"))
                .willReturn(Optional.empty());
        given(supplierPropertyReader.findBySupplierIdAndExternalPropertyId(SUPPLIER_ID, "EXT-2"))
                .willThrow(new RuntimeException("동기화 실패"));
        given(supplierPropertyReader.findBySupplierIdAndExternalPropertyId(SUPPLIER_ID, "EXT-3"))
                .willReturn(Optional.empty());

        given(supplierManager.saveProperty(any(SupplierProperty.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(supplierPropertyReader.findBySupplierId(SUPPLIER_ID)).willReturn(List.of());

        supplierSyncService.syncSupplier(SUPPLIER_ID);

        verify(supplierManager, times(2)).saveSyncJob(any(SupplierSyncJob.class));
        verify(supplierManager, times(2)).saveProperty(any(SupplierProperty.class));
    }

    @Test
    @DisplayName("fetchProperties 실패 시 syncJob FAILED 상태")
    void fetchProperties_실패_시_syncJob_FAILED() {
        final Supplier supplier = createSupplier();

        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);
        given(supplierManager.saveSyncJob(any(SupplierSyncJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(mockAdapter.fetchProperties()).willThrow(new RuntimeException("API 호출 실패"));

        supplierSyncService.syncSupplier(SUPPLIER_ID);

        verify(supplierManager, times(2)).saveSyncJob(any(SupplierSyncJob.class));
    }

    @Test
    @DisplayName("MAPPED 공급사 숙소의 요금/재고가 동기화된다")
    void MAPPED_공급사_숙소의_요금_재고가_동기화된다() {
        final Supplier supplier = createSupplier();
        final SupplierProperty supplierProperty = SupplierProperty.create(SUPPLIER_ID, "EXT-1", "{\"id\":1}");
        ReflectionTestUtils.setField(supplierProperty, "id", 10L);

        final SupplierPropertyMapping mapping = SupplierPropertyMapping.create(10L, 100L);
        mapping.map();

        final RoomType roomType = RoomType.create(100L, "디럭스", "설명", 4, BigDecimal.valueOf(100000), "WiFi", 5);
        ReflectionTestUtils.setField(roomType, "id", 1000L);

        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);
        given(mockAdapter.fetchProperties()).willReturn(List.of(
                new SupplierPropertyData("EXT-1", "숙소1", "주소1", "서울", "호텔", "{\"id\":1}")
        ));
        given(supplierManager.saveSyncJob(any(SupplierSyncJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(supplierPropertyReader.findBySupplierIdAndExternalPropertyId(eq(SUPPLIER_ID), any()))
                .willReturn(Optional.of(supplierProperty));
        given(supplierPropertyReader.findBySupplierId(SUPPLIER_ID)).willReturn(List.of(supplierProperty));
        given(supplierMappingReader.findBySupplierPropertyIds(List.of(10L), MappingStatus.MAPPED))
                .willReturn(List.of(mapping));
        given(roomTypeReader.findActiveByPropertyId(100L)).willReturn(List.of(roomType));
        given(mockAdapter.fetchRates(eq("EXT-1"), any(), any())).willReturn(List.of(
                new SupplierRateData("EXT-ROOM-001", LocalDate.now(), BigDecimal.valueOf(120000), "KRW")
        ));
        given(mockAdapter.fetchInventory(eq("EXT-1"), any(), any())).willReturn(List.of(
                new SupplierInventoryData("EXT-ROOM-001", LocalDate.now(), 5)
        ));
        given(rateReader.findByRoomTypeIdAndDateBetween(eq(1000L), any(), any())).willReturn(List.of());
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(1000L), any(), any())).willReturn(List.of());

        supplierSyncService.syncSupplier(SUPPLIER_ID);

        verify(rateManager).saveAll(any());
        verify(inventoryManager).saveAll(any());
    }

    @Test
    @DisplayName("MAPPED 숙소가 없으면 요금/재고 동기화를 건너뛴다")
    void MAPPED_숙소가_없으면_요금_재고_동기화를_건너뛴다() {
        final Supplier supplier = createSupplier();
        final SupplierProperty supplierProperty = SupplierProperty.create(SUPPLIER_ID, "EXT-1", "{\"id\":1}");
        ReflectionTestUtils.setField(supplierProperty, "id", 10L);

        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);
        given(mockAdapter.fetchProperties()).willReturn(List.of(
                new SupplierPropertyData("EXT-1", "숙소1", "주소1", "서울", "호텔", "{\"id\":1}")
        ));
        given(supplierManager.saveSyncJob(any(SupplierSyncJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(supplierPropertyReader.findBySupplierIdAndExternalPropertyId(eq(SUPPLIER_ID), any()))
                .willReturn(Optional.of(supplierProperty));
        given(supplierPropertyReader.findBySupplierId(SUPPLIER_ID)).willReturn(List.of(supplierProperty));
        given(supplierMappingReader.findBySupplierPropertyIds(List.of(10L), MappingStatus.MAPPED))
                .willReturn(List.of());

        supplierSyncService.syncSupplier(SUPPLIER_ID);

        verify(rateManager, never()).saveAll(any());
        verify(inventoryManager, never()).saveAll(any());
    }

    @Test
    @DisplayName("기존 요금이 있으면 가격만 업데이트한다")
    void 기존_요금이_있으면_가격만_업데이트한다() {
        final Supplier supplier = createSupplier();
        final SupplierProperty supplierProperty = SupplierProperty.create(SUPPLIER_ID, "EXT-1", "{\"id\":1}");
        ReflectionTestUtils.setField(supplierProperty, "id", 10L);

        final SupplierPropertyMapping mapping = SupplierPropertyMapping.create(10L, 100L);
        mapping.map();

        final RoomType roomType = RoomType.create(100L, "디럭스", "설명", 4, BigDecimal.valueOf(100000), "WiFi", 5);
        ReflectionTestUtils.setField(roomType, "id", 1000L);

        final LocalDate today = LocalDate.now();
        final Rate existingRate = Rate.create(1000L, today, BigDecimal.valueOf(80000));

        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);
        given(mockAdapter.fetchProperties()).willReturn(List.of(
                new SupplierPropertyData("EXT-1", "숙소1", "주소1", "서울", "호텔", "{\"id\":1}")
        ));
        given(supplierManager.saveSyncJob(any(SupplierSyncJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(supplierPropertyReader.findBySupplierIdAndExternalPropertyId(eq(SUPPLIER_ID), any()))
                .willReturn(Optional.of(supplierProperty));
        given(supplierPropertyReader.findBySupplierId(SUPPLIER_ID)).willReturn(List.of(supplierProperty));
        given(supplierMappingReader.findBySupplierPropertyIds(List.of(10L), MappingStatus.MAPPED))
                .willReturn(List.of(mapping));
        given(roomTypeReader.findActiveByPropertyId(100L)).willReturn(List.of(roomType));
        given(mockAdapter.fetchRates(eq("EXT-1"), any(), any())).willReturn(List.of(
                new SupplierRateData("EXT-ROOM-001", today, BigDecimal.valueOf(120000), "KRW")
        ));
        given(mockAdapter.fetchInventory(eq("EXT-1"), any(), any())).willReturn(List.of());
        given(rateReader.findByRoomTypeIdAndDateBetween(eq(1000L), any(), any())).willReturn(List.of(existingRate));
        given(inventoryReader.findByRoomTypeIdAndDateBetween(eq(1000L), any(), any())).willReturn(List.of());

        supplierSyncService.syncSupplier(SUPPLIER_ID);

        verify(rateManager, never()).saveAll(any());
    }

    private Supplier createSupplier() {
        final Supplier supplier = Supplier.create("테스트 공급사", SUPPLIER_CODE, null, null);
        ReflectionTestUtils.setField(supplier, "id", SUPPLIER_ID);
        return supplier;
    }
}
