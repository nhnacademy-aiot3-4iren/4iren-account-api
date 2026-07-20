package com.nhnacademy.accountapi.repository;

import com.nhnacademy.accountapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserLoginId(String userLoginId);

    Optional<User> findByUserId(Long userId);
    Optional<User> findByUserEmail(String userEmail);

    boolean existsByUserLoginId(String userLoginId);
    boolean existsByUserEmail(String userEmail);

}
