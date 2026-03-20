package com.rentify.core.repository;

import com.rentify.core.entity.User;
import com.rentify.core.enums.SubscriptionPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);
    Optional<User> findByOauthProviderAndOauthSubject(String oauthProvider, String oauthSubject);
    Optional<User> findByIdAndIsActiveTrue(Long id);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.subscriptionPlan = :freePlan, u.subscriptionActiveUntil = null " +
            "WHERE u.subscriptionActiveUntil IS NOT NULL " +
            "AND u.subscriptionActiveUntil <= :now " +
            "AND u.subscriptionPlan <> :freePlan")
    int resetExpiredSubscriptions(
            @Param("freePlan") SubscriptionPlan freePlan,
            @Param("now") ZonedDateTime now
    );
}
