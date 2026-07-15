package com.nhnacademy.accountapi.repository;

import com.nhnacademy.accountapi.entity.Member;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUserLoginId(String userLoginId);

    Optional<Member> findByUserId(String userId);
    Optional<Member> findByUserEmail(String userEmail);

    boolean existsByUserLoginId(String userLoginId);
    boolean existsByUserEmail(String userEmail);

}
