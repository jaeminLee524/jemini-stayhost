package com.jemini.stayhost.channel.domain.dto;

public record ChannelSyncResult(
    boolean success,
    String channelCode,
    String errorMessage,
    int retryCount
) {

    public static ChannelSyncResult success(final String channelCode) {
        return new ChannelSyncResult(true, channelCode, null, 0);
    }

    public static ChannelSyncResult failure(final String channelCode, final String errorMessage, final int retryCount) {
        return new ChannelSyncResult(false, channelCode, errorMessage, retryCount);
    }
}
