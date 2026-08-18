package com.nhnacademy.accountapi.repository;

import com.nhnacademy.accountapi.dto.UserResponse;
import com.nhnacademy.accountapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    List<User> getUsersByCreatedBy(Long requesterId);
}
