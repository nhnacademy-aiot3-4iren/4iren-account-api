package com.nhnacademy.accountapi.repository;

import com.nhnacademy.accountapi.dto.internal.UserRoleResponse;
import com.nhnacademy.accountapi.dto.internal.UserStatusResponse;
import com.nhnacademy.accountapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    List<User> getUsersByCreatedBy(Long requesterId);

    Optional<UserRoleResponse> findRoleByUserId(Long userId);

    Optional<UserStatusResponse> findStatusByUserId(Long userId);

    List<UserStatusResponse> findStatusesByUserIdIn(Collection<Long> userIds);

    @Query("SELECT u.email FROM User u WHERE u.userId = :userId")
    Optional<String> findEmailByUserId(@Param("userId") Long userId);
}
