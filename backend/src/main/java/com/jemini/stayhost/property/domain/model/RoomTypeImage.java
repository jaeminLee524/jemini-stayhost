package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room_type_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomTypeImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    public static RoomTypeImage create(final RoomType roomType, final String imageUrl, final int sortOrder) {
        final RoomTypeImage image = new RoomTypeImage();
        image.roomType = roomType;
        image.imageUrl = imageUrl;
        image.sortOrder = sortOrder;
        return image;
    }
}
