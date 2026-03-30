package com.jemini.stayhost.channel.infrastructure.persistence;

import com.jemini.stayhost.channel.domain.model.Channel;
import com.jemini.stayhost.channel.domain.model.ChannelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByCode(String code);

    List<Channel> findByStatus(ChannelStatus status);
}
