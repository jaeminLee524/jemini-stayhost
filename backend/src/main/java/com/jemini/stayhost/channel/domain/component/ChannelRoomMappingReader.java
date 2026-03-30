package com.jemini.stayhost.channel.domain.component;

import com.jemini.stayhost.channel.domain.model.ChannelRoomMapping;

import java.util.List;

public interface ChannelRoomMappingReader {

    List<ChannelRoomMapping> findByChannelPropertyMappingId(Long channelPropertyMappingId);
}
