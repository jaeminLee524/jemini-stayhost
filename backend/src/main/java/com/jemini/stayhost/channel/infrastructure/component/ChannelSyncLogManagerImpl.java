package com.jemini.stayhost.channel.infrastructure.component;

import com.jemini.stayhost.channel.domain.component.ChannelSyncLogManager;
import com.jemini.stayhost.channel.domain.model.ChannelSyncLog;
import com.jemini.stayhost.channel.infrastructure.persistence.ChannelSyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelSyncLogManagerImpl implements ChannelSyncLogManager {

    private final ChannelSyncLogRepository channelSyncLogRepository;

    @Override
    public ChannelSyncLog save(final ChannelSyncLog syncLog) {
        return channelSyncLogRepository.save(syncLog);
    }
}
