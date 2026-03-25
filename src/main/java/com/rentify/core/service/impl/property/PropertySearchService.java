package com.rentify.core.service.impl.property;

import com.rentify.core.dto.property.PropertyMapPinDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.Property;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.specification.PropertySpecifications;
import com.rentify.core.validation.PropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertySearchService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final PropertyValidator propertyValidator;

    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> search(PropertySearchCriteriaDto criteria, Pageable pageable) {
        propertyValidator.validateSearchCriteria(criteria);
        return propertyRepository.findAll(PropertySpecifications.withFilters(criteria), withTopPrioritySort(pageable))
                .map(propertyMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PropertyMapPinDto> searchMapPins(PropertySearchCriteriaDto criteria, Pageable pageable) {
        propertyValidator.validateSearchCriteria(criteria);
        Specification<Property> mapSpecification = PropertySpecifications.withFilters(criteria)
                .and((root, query, cb) -> cb.and(
                        cb.isNotNull(root.get("address").get("lat")),
                        cb.isNotNull(root.get("address").get("lng"))
                ));
        return propertyRepository.findAll(mapSpecification, withTopPrioritySort(pageable))
                .map(propertyMapper::toMapPinDto);
    }

    private Pageable withTopPrioritySort(Pageable pageable) {
        Sort topPrioritySort = Sort.by(
                Sort.Order.desc("isTopPromoted"),
                Sort.Order.desc("topPromotedUntil")
        );
        Sort finalSort = pageable.getSort().isSorted()
                ? topPrioritySort.and(pageable.getSort())
                : topPrioritySort.and(Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), finalSort);
    }
}
