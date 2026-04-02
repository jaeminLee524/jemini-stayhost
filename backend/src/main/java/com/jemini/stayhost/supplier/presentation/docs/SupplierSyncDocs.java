package com.jemini.stayhost.supplier.presentation.docs;

import com.jemini.stayhost.common.response.ApiBaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Supplier 동기화", description = "외부 공급사 데이터 동기화 API")
public interface SupplierSyncDocs {

    @Operation(summary = "공급사 데이터 동기화", description = """
        지정된 공급사의 숙소·요금·재고 데이터를 외부 시스템으로부터 동기화한다.
        ``` json
        [ERROR_CODE]
        * SUPPLIER_NOT_FOUND: 공급사를 찾을 수 없음
        ```
        """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "동기화 성공"),
        @ApiResponse(responseCode = "404", description = "공급사 없음")
    })
    ApiBaseResponse<Void> syncSupplier(
        @Parameter(description = "공급사 ID", example = "1") @PathVariable Long supplierId
    );
}
