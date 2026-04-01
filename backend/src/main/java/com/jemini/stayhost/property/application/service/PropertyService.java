package com.jemini.stayhost.property.application.service;

import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.application.dto.PropertyCreateCommand;
import com.jemini.stayhost.property.application.dto.PropertyResult;
import com.jemini.stayhost.property.application.dto.PropertyUpdateCommand;
import com.jemini.stayhost.property.domain.component.PropertyManager;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.event.PropertyUpdatedEvent;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyStatus;
import com.jemini.stayhost.property.domain.model.PropertyType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyReader propertyReader;
    private final PropertyManager propertyManager;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 숙소 활성 상태 검증. 비활성이면 예외를 던진다.
     */
    @Transactional(readOnly = true)
    public void validatePropertyActive(final Long propertyId) {
        final Property property = propertyReader.getById(propertyId);
        property.validateActive();
    }

    /**
     * 숙소 등록. INACTIVE 상태로 생성된다.
     */
    @Transactional
    public PropertyResult createProperty(final Long partnerId, final PropertyCreateCommand command) {
        final Property property = buildProperty(partnerId, command);
        final Property saved = propertyManager.save(property);

        return PropertyResult.from(saved);
    }

    /**
     * 내 숙소 목록 조회 (페이지네이션).
     */
    @Transactional(readOnly = true)
    public PageResult<PropertyResult> getMyProperties(final Long partnerId, final Pageable pageable) {
        return PageResult.from(propertyReader.findByPartnerId(partnerId, pageable)
            .map(PropertyResult::from));
    }

    /**
     * 숙소 상세 조회. 소유권을 검증한다.
     */
    @Transactional(readOnly = true)
    public PropertyResult getProperty(final Long propertyId, final Long partnerId) {
        final Property property = propertyReader.getById(propertyId);

        property.validateOwner(partnerId);

        return PropertyResult.from(property);
    }

    /**
     * 숙소 정보 수정. 소유권을 검증한다.
     */
    @Transactional
    public PropertyResult updateProperty(final Long propertyId, final Long partnerId, final PropertyUpdateCommand command) {
        final Property property = propertyReader.getById(propertyId);

        property.validateOwner(partnerId);
        property.update(command.name(), command.description(), command.checkInTime(), command.checkOutTime(), command.thumbnailUrl());

        eventPublisher.publishEvent(PropertyUpdatedEvent.create(propertyId));

        return PropertyResult.from(property);
    }

    /**
     * 숙소 상태 변경. 소유권을 검증한다.
     */
    @Transactional
    public PropertyResult changeStatus(final Long propertyId, final Long partnerId, final PropertyStatus status) {
        final Property property = propertyReader.getById(propertyId);

        property.validateOwner(partnerId);
        property.changeStatus(status);

        eventPublisher.publishEvent(PropertyUpdatedEvent.create(propertyId));

        return PropertyResult.from(property);
    }

    private Property buildProperty(final Long partnerId, final PropertyCreateCommand command) {
        return Property.create(
            partnerId,
            command.name(),
            PropertyType.valueOf(command.type()),
            command.description(),
            command.address(),
            command.region(),
            command.checkInTime(),
            command.checkOutTime(),
            command.latitude(),
            command.longitude(),
            command.thumbnailUrl()
        );
    }
}
