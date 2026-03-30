package com.jemini.stayhost.channel.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "channel_room_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelRoomMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long channelPropertyMappingId;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false, length = 100)
    private String externalRoomId;

    public static ChannelRoomMapping create(
        final Long channelPropertyMappingId,
        final Long roomTypeId,
        final String externalRoomId
    ) {
        final ChannelRoomMapping mapping = new ChannelRoomMapping();
        mapping.channelPropertyMappingId = channelPropertyMappingId;
        mapping.roomTypeId = roomTypeId;
        mapping.externalRoomId = externalRoomId;
        return mapping;
    }
}
