package com.rentify.core.repository;

import com.rentify.core.entity.LongTermRequest;
import com.rentify.core.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LongTermRequestRepository extends JpaRepository<LongTermRequest, Long> {
    List<LongTermRequest> findAllByPropertyId(Long propertyId);
    List<LongTermRequest> findAllByTenantId(Long tenantId);
    List<LongTermRequest> findAllByPropertyIdAndStatus(Long propertyId, RequestStatus status);
    boolean existsByPropertyId(Long propertyId);
}
