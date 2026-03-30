package com.jemini.stayhost.channel.infrastructure.component;

import com.jemini.stayhost.channel.domain.component.ChannelMappingReader;
import com.jemini.stayhost.channel.domain.model.ChannelPropertyMapping;
import com.jemini.stayhost.channel.domain.model.ChannelStatus;
import com.jemini.stayhost.channel.infrastructure.persistence.ChannelPropertyMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelMappingReaderImpl implements ChannelMappingReader {

    private final ChannelPropertyMappingRepository channelPropertyMappingRepository;

    @Override
    public List<ChannelPropertyMapping> findActiveByPropertyId(final Long propertyId) {
        return channelPropertyMappingRepository.findByPropertyIdAndStatus(propertyId, ChannelStatus.ACTIVE);
    }
}
