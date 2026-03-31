package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyStatus;
import com.jemini.stayhost.property.infrastructure.persistence.PropertyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class PropertyReaderImpl implements PropertyReader {

    private final PropertyRepository propertyRepository;
    private final EntityManager entityManager;

    @Override
    public Property getById(final Long id) {
        return propertyRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    @Cacheable(value = "property", key = "#id")
    @Override
    public Property getActiveById(final Long id) {
        return propertyRepository.findByIdAndStatus(id, PropertyStatus.ACTIVE)
            .orElseThrow(() -> new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    @Override
    public Page<Property> findByPartnerId(final Long partnerId, final Pageable pageable) {
        return propertyRepository.findByPartnerId(partnerId, pageable);
    }

    @Override
    public Page<Property> searchActive(final String region, final String keyword, final Pageable pageable) {
        final String jpql = """
            SELECT p FROM Property p
            WHERE p.status = :status
            AND (:region IS NULL OR p.region = :region)
            AND (:keyword IS NULL OR p.name LIKE :keyword)
            ORDER BY p.name ASC
            """;
        final String countJpql = """
            SELECT COUNT(p) FROM Property p
            WHERE p.status = :status
            AND (:region IS NULL OR p.region = :region)
            AND (:keyword IS NULL OR p.name LIKE :keyword)
            """;

        final String keywordParam = isNotBlank(keyword) ? keyword + "%" : null;

        final TypedQuery<Property> query = entityManager.createQuery(jpql, Property.class)
            .setParameter("status", PropertyStatus.ACTIVE)
            .setParameter("region", isNotBlank(region) ? region : null)
            .setParameter("keyword", keywordParam)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

        final TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class)
            .setParameter("status", PropertyStatus.ACTIVE)
            .setParameter("region", isNotBlank(region) ? region : null)
            .setParameter("keyword", keywordParam);

        return new PageImpl<>(query.getResultList(), pageable, countQuery.getSingleResult());
    }
}
