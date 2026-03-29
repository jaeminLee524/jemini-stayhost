package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rate", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_type_id", "date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rate extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long roomTypeId;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  public static Rate create(final Long roomTypeId, final LocalDate date, final BigDecimal price) {
    final Rate rate = new Rate();
    rate.roomTypeId = roomTypeId;
    rate.date = date;
    rate.price = price;
    return rate;
  }

  public void updatePrice(final BigDecimal price) {
    this.price = price;
  }
}
