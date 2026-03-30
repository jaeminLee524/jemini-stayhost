package com.jemini.stayhost.supplier.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supplier")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Supplier extends BaseEntity {

    private static final int DEFAULT_SYNC_INTERVAL = 3600;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(length = 500)
    private String apiBaseUrl;

    @Column(length = 255)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncType syncType;

    private Integer syncInterval;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupplierStatus status;

    public static Supplier create(
        final String name,
        final String code,
        final String apiBaseUrl,
        final String apiKey
    ) {
        final Supplier supplier = new Supplier();
        supplier.name = name;
        supplier.code = code;
        supplier.apiBaseUrl = apiBaseUrl;
        supplier.apiKey = apiKey;
        supplier.syncType = SyncType.PULL;
        supplier.syncInterval = DEFAULT_SYNC_INTERVAL;
        supplier.status = SupplierStatus.ACTIVE;
        return supplier;
    }
}
