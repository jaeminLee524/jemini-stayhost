package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_type_id", "date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer totalCount;

    @Column(nullable = false)
    private Integer reservedCount;

    public static Inventory create(final Long roomTypeId, final LocalDate date, final int totalCount) {
        final Inventory inventory = new Inventory();
        inventory.roomTypeId = roomTypeId;
        inventory.date = date;
        inventory.totalCount = totalCount;
        inventory.reservedCount = 0;
        return inventory;
    }

    public int getAvailableCount() {
        return this.totalCount - this.reservedCount;
    }

    public void decreaseStock() {
        if (getAvailableCount() <= 0) {
            throw new BusinessException(ErrorCode.INVENTORY_INSUFFICIENT);
        }
        this.reservedCount++;
    }

    public void increaseStock() {
        if (this.reservedCount <= 0) {
            return;
        }
        this.reservedCount--;
    }

    public void updateTotalCount(final int totalCount) {
        if (totalCount < this.reservedCount) {
            throw new BusinessException(ErrorCode.INVENTORY_TOTAL_BELOW_RESERVED);
        }
        this.totalCount = totalCount;
    }
}
