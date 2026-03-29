package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "room_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomType extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long propertyId;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private Integer maxOccupancy;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal basePrice;

  @Column(columnDefinition = "JSON")
  private String amenities;

  @Column(nullable = false)
  private Integer totalRoomCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RoomTypeStatus status;

  public static RoomType create(
      final Long propertyId,
      final String name,
      final String description,
      final int maxOccupancy,
      final BigDecimal basePrice,
      final String amenities,
      final int totalRoomCount
  ) {
    final RoomType roomType = new RoomType();
    roomType.propertyId = propertyId;
    roomType.name = name;
    roomType.description = description;
    roomType.maxOccupancy = maxOccupancy;
    roomType.basePrice = basePrice;
    roomType.amenities = amenities;
    roomType.totalRoomCount = totalRoomCount;
    roomType.status = RoomTypeStatus.ACTIVE;
    return roomType;
  }

  public void update(final String name, final String description, final int maxOccupancy, final BigDecimal basePrice) {
    this.name = name;
    this.description = description;
    this.maxOccupancy = maxOccupancy;
    this.basePrice = basePrice;
  }

  public void validateGuestCount(final int guestCount) {
    if (guestCount > this.maxOccupancy) {
      throw new BusinessException(ErrorCode.INVALID_GUEST_COUNT);
    }
  }
}
