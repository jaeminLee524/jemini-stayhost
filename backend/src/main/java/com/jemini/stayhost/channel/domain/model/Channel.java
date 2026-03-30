package com.jemini.stayhost.channel.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "channel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(length = 500)
    private String apiBaseUrl;

    @Column(length = 255)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelStatus status;

    public static Channel create(
        final String name,
        final String code,
        final String apiBaseUrl,
        final String apiKey
    ) {
        final Channel channel = new Channel();
        channel.name = name;
        channel.code = code;
        channel.apiBaseUrl = apiBaseUrl;
        channel.apiKey = apiKey;
        channel.status = ChannelStatus.ACTIVE;
        return channel;
    }
}
