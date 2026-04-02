package com.jemini.stayhost.property.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoomTypeImageTest {

    @Test
    @DisplayName("RoomTypeImage 생성 성공")
    void RoomTypeImage_생성_성공() {
        final RoomType roomType = createRoomType();

        final RoomTypeImage image = RoomTypeImage.create(roomType, "https://cdn.example.com/img/1.jpg", 0);

        assertThat(image.getRoomType()).isEqualTo(roomType);
        assertThat(image.getImageUrl()).isEqualTo("https://cdn.example.com/img/1.jpg");
        assertThat(image.getSortOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("RoomType 이미지 교체 성공")
    void RoomType_이미지_교체_성공() {
        final RoomType roomType = createRoomType();
        roomType.replaceImages(List.of("https://cdn.example.com/img/1.jpg"));

        roomType.replaceImages(List.of("https://cdn.example.com/img/2.jpg", "https://cdn.example.com/img/3.jpg"));

        assertThat(roomType.getImages()).hasSize(2);
        assertThat(roomType.getImages().get(0).getImageUrl()).isEqualTo("https://cdn.example.com/img/2.jpg");
        assertThat(roomType.getImages().get(0).getSortOrder()).isEqualTo(0);
        assertThat(roomType.getImages().get(1).getImageUrl()).isEqualTo("https://cdn.example.com/img/3.jpg");
        assertThat(roomType.getImages().get(1).getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("RoomType 이미지 전체 삭제")
    void RoomType_이미지_전체_삭제() {
        final RoomType roomType = createRoomType();
        roomType.replaceImages(List.of("https://cdn.example.com/img/1.jpg"));

        roomType.replaceImages(List.of());

        assertThat(roomType.getImages()).isEmpty();
    }

    private RoomType createRoomType() {
        return RoomType.create(1L, "스탠다드 더블", "설명", 2, BigDecimal.valueOf(120000), null, 10);
    }
}
