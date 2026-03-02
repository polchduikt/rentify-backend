package com.rentify.core.repository;

import com.rentify.core.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByHostId(Long hostId);
    List<Conversation> findAllByTenantId(Long tenantId);
    Optional<Conversation> findByPropertyIdAndHostIdAndTenantId(Long propertyId, Long hostId, Long tenantId);
}