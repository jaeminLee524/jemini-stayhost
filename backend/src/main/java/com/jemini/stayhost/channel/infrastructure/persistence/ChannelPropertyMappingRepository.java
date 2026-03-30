package com.jemini.stayhost.channel.infrastructure.persistence;

import com.jemini.stayhost.channel.domain.model.ChannelPropertyMapping;
import com.jemini.stayhost.channel.domain.model.ChannelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelPropertyMappingRepository extends JpaRepository<ChannelPropertyMapping, Long> {

    List<ChannelPropertyMapping> findByPropertyIdAndStatus(Long propertyId, ChannelStatus status);
}
