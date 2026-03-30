package com.jemini.stayhost.channel.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "channel_rate_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelRatePolicy extends BaseEntity {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long channelPropertyMappingId;

    @Column(nullable = false)
    private Long roomTypeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MarkupType markupType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal markupValue;

    public static ChannelRatePolicy create(
        final Long channelPropertyMappingId,
        final Long roomTypeId,
        final MarkupType markupType,
        final BigDecimal markupValue
    ) {
        final ChannelRatePolicy policy = new ChannelRatePolicy();
        policy.channelPropertyMappingId = channelPropertyMappingId;
        policy.roomTypeId = roomTypeId;
        policy.markupType = markupType;
        policy.markupValue = markupValue;
        return policy;
    }

    public BigDecimal applyMarkup(final BigDecimal basePrice) {
        return switch (this.markupType) {
            case PERCENTAGE -> basePrice.multiply(BigDecimal.ONE.add(this.markupValue.divide(HUNDRED)));
            case FIXED -> basePrice.add(this.markupValue);
        };
    }
}
