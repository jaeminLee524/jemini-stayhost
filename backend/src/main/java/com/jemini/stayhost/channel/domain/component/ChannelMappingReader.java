package com.jemini.stayhost.channel.domain.component;

import com.jemini.stayhost.channel.domain.model.ChannelPropertyMapping;

import java.util.List;

public interface ChannelMappingReader {

    List<ChannelPropertyMapping> findActiveByPropertyId(Long propertyId);
}
