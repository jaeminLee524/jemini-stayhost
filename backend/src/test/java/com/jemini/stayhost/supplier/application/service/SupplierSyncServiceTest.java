package com.jemini.stayhost.supplier.application.service;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.supplier.domain.component.SupplierAdapter;
import com.jemini.stayhost.supplier.domain.component.SupplierManager;
import com.jemini.stayhost.supplier.domain.component.SupplierPropertyReader;
import com.jemini.stayhost.supplier.domain.component.SupplierReader;
import com.jemini.stayhost.supplier.domain.dto.SupplierPropertyData;
import com.jemini.stayhost.supplier.domain.model.Supplier;
import com.jemini.stayhost.supplier.domain.model.SupplierProperty;
import com.jemini.stayhost.supplier.domain.model.SupplierSyncJob;
import com.jemini.stayhost.supplier.domain.model.SyncJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
    private SupplierManager supplierManager;

    @Mock
    private SupplierAdapter mockAdapter;

    private static final Long SUPPLIER_ID = 1L;
    private static final String SUPPLIER_CODE = "MOCK_SUPPLIER";

    @BeforeEach
    void setUp() {
        given(mockAdapter.getSupplierCode()).willReturn(SUPPLIER_CODE);
        supplierSyncService = new SupplierSyncService(
                supplierReader,
                supplierPropertyReader,
                supplierManager,
                List.of(mockAdapter)
        );
    }

    @Test
    @DisplayName("동기화 성공 - 3건 모두 신규 저장")
    void 동기화_성공() {
        // given
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

        // when
        supplierSyncService.syncSupplier(SUPPLIER_ID);

        // then
        verify(supplierManager, times(2)).saveSyncJob(any(SupplierSyncJob.class));
        verify(supplierManager, times(3)).saveProperty(any(SupplierProperty.class));
        verify(supplierPropertyReader, times(3))
                .findBySupplierIdAndExternalPropertyId(eq(SUPPLIER_ID), any());
    }

    @Test
    @DisplayName("어댑터 없으면 예외 발생")
    void 어댑터_없으면_예외() {
        // given
        final Supplier supplier = Supplier.create("다른 공급사", "UNKNOWN_CODE", null, null);
        ReflectionTestUtils.setField(supplier, "id", SUPPLIER_ID);
        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);

        // when & then
        assertThatThrownBy(() -> supplierSyncService.syncSupplier(SUPPLIER_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUPPLIER_ADAPTER_NOT_FOUND);
    }

    @Test
    @DisplayName("기존 숙소가 존재하면 rawData 업데이트")
    void 기존_숙소_업데이트() {
        // given
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

        // when
        supplierSyncService.syncSupplier(SUPPLIER_ID);

        // then
        verify(supplierManager, never()).saveProperty(any(SupplierProperty.class));
        verify(supplierManager, times(2)).saveSyncJob(any(SupplierSyncJob.class));
    }

    @Test
    @DisplayName("부분 실패 시 성공/실패 건수 추적")
    void 부분_실패_시_성공_실패_건수_추적() {
        // given
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

        // when
        supplierSyncService.syncSupplier(SUPPLIER_ID);

        // then
        verify(supplierManager, times(2)).saveSyncJob(any(SupplierSyncJob.class));
        verify(supplierManager, times(2)).saveProperty(any(SupplierProperty.class));
    }

    @Test
    @DisplayName("fetchProperties 실패 시 syncJob FAILED 상태")
    void fetchProperties_실패_시_syncJob_FAILED() {
        // given
        final Supplier supplier = createSupplier();

        given(supplierReader.getById(SUPPLIER_ID)).willReturn(supplier);
        given(supplierManager.saveSyncJob(any(SupplierSyncJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(mockAdapter.fetchProperties()).willThrow(new RuntimeException("API 호출 실패"));

        // when
        supplierSyncService.syncSupplier(SUPPLIER_ID);

        // then
        verify(supplierManager, times(2)).saveSyncJob(any(SupplierSyncJob.class));
    }

    private Supplier createSupplier() {
        final Supplier supplier = Supplier.create("테스트 공급사", SUPPLIER_CODE, null, null);
        ReflectionTestUtils.setField(supplier, "id", SUPPLIER_ID);
        return supplier;
    }
}
