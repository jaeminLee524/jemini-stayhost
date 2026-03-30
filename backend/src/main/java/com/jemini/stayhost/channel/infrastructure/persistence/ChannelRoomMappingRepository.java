package com.jemini.stayhost.channel.infrastructure.persistence;

import com.jemini.stayhost.channel.domain.model.ChannelRoomMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelRoomMappingRepository extends JpaRepository<ChannelRoomMapping, Long> {

    List<ChannelRoomMapping> findByChannelPropertyMappingId(Long channelPropertyMappingId);
}
