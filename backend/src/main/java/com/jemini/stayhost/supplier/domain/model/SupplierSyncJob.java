package com.jemini.stayhost.supplier.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_sync_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplierSyncJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long supplierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SyncJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncJobStatus status;

    @Column(nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int failCount;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    public static SupplierSyncJob start(
        final Long supplierId,
        final SyncJobType jobType
    ) {
        final SupplierSyncJob job = new SupplierSyncJob();
        job.supplierId = supplierId;
        job.jobType = jobType;
        job.status = SyncJobStatus.RUNNING;
        job.startedAt = LocalDateTime.now();
        return job;
    }

    public void complete(
        final int totalCount,
        final int successCount,
        final int failCount
    ) {
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failCount = failCount;
        this.status = SyncJobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(final String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = SyncJobStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }
}
