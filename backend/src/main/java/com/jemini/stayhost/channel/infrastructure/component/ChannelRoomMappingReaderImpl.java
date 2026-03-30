package com.jemini.stayhost.channel.infrastructure.component;

import com.jemini.stayhost.channel.domain.component.ChannelRoomMappingReader;
import com.jemini.stayhost.channel.domain.model.ChannelRoomMapping;
import com.jemini.stayhost.channel.infrastructure.persistence.ChannelRoomMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelRoomMappingReaderImpl implements ChannelRoomMappingReader {

    private final ChannelRoomMappingRepository channelRoomMappingRepository;

    @Override
    public List<ChannelRoomMapping> findByChannelPropertyMappingId(final Long channelPropertyMappingId) {
        return channelRoomMappingRepository.findByChannelPropertyMappingId(channelPropertyMappingId);
    }
}
