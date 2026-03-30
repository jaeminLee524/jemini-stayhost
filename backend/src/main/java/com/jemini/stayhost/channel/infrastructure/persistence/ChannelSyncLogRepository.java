package com.jemini.stayhost.channel.infrastructure.persistence;

import com.jemini.stayhost.channel.domain.model.ChannelSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelSyncLogRepository extends JpaRepository<ChannelSyncLog, Long> {
}
