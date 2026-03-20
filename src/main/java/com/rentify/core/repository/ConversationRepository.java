package com.rentify.core.repository;

import com.rentify.core.entity.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByPropertyIdAndTenantId(Long propertyId, Long tenantId);
    boolean existsByPropertyId(Long propertyId);
    @EntityGraph(attributePaths = {"property", "host", "tenant"})
    @Query("SELECT c FROM Conversation c WHERE c.host.id = :userId OR c.tenant.id = :userId ORDER BY c.createdAt DESC")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);
}
