package com.jemini.stayhost.channel.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "channel_property_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelPropertyMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long channelId;

    @Column(nullable = false)
    private Long propertyId;

    @Column(nullable = false, length = 100)
    private String externalPropertyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelStatus status;

    public static ChannelPropertyMapping create(
        final Long channelId,
        final Long propertyId,
        final String externalPropertyId
    ) {
        final ChannelPropertyMapping mapping = new ChannelPropertyMapping();
        mapping.channelId = channelId;
        mapping.propertyId = propertyId;
        mapping.externalPropertyId = externalPropertyId;
        mapping.status = ChannelStatus.ACTIVE;
        return mapping;
    }
}
