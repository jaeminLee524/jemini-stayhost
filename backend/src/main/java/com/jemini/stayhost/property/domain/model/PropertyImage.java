package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "property_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PropertyImage extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  @Column(nullable = false, length = 500)
  private String imageUrl;

  @Column(nullable = false)
  private Integer sortOrder;

  public static PropertyImage create(final Property property, final String imageUrl, final int sortOrder) {
    final PropertyImage image = new PropertyImage();
    image.property = property;
    image.imageUrl = imageUrl;
    image.sortOrder = sortOrder;
    return image;
  }
}
