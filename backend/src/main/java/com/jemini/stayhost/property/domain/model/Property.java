package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import com.jemini.stayhost.common.exception.AuthorizationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "property")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Property extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PropertyType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @Column(length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyStatus status;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyImage> images = new ArrayList<>();

    public static Property create(
            final Long partnerId,
            final String name,
            final PropertyType type,
            final String description,
            final String address,
            final String region,
            final LocalTime checkInTime,
            final LocalTime checkOutTime
    ) {
        final Property property = new Property();
        property.partnerId = partnerId;
        property.name = name;
        property.type = type;
        property.description = description;
        property.address = address;
        property.region = region;
        property.checkInTime = checkInTime;
        property.checkOutTime = checkOutTime;
        property.status = PropertyStatus.INACTIVE;
        return property;
    }

    public void update(
            final String name,
            final String description,
            final java.time.LocalTime checkInTime,
            final java.time.LocalTime checkOutTime,
            final String thumbnailUrl
    ) {
        this.name = name;
        this.description = description;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void changeStatus(final PropertyStatus status) {
        this.status = status;
    }

    public void validateOwner(final Long requestingPartnerId) {
        if (!this.partnerId.equals(requestingPartnerId)) {
            throw new AuthorizationException("해당 숙소에 대한 권한이 없습니다.");
        }
    }
}
