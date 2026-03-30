package com.jemini.stayhost.channel.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "channel_sync_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelSyncLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long channelId;

    private Long propertyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChannelSyncType syncType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SyncDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelSyncStatus status;

    @Column(columnDefinition = "json")
    private String requestPayload;

    @Column(columnDefinition = "json")
    private String responsePayload;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private Integer retryCount;

    public static ChannelSyncLog createOutbound(
        final Long channelId,
        final Long propertyId,
        final ChannelSyncType syncType
    ) {
        final ChannelSyncLog log = new ChannelSyncLog();
        log.channelId = channelId;
        log.propertyId = propertyId;
        log.syncType = syncType;
        log.direction = SyncDirection.OUTBOUND;
        log.status = ChannelSyncStatus.SUCCESS;
        log.retryCount = 0;
        return log;
    }

    public void markFailed(final String errorMessage) {
        this.status = ChannelSyncStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void incrementRetry() {
        this.retryCount++;
    }
}
