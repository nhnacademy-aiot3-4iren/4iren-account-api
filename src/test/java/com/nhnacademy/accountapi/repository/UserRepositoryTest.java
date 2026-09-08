package com.nhnacademy.accountapi.repository;

import com.nhnacademy.accountapi.entity.User;
import com.nhnacademy.accountapi.entity.UserRole;
import com.nhnacademy.accountapi.entity.UserStatus;
import com.nhnacademy.accountapi.dto.internal.UserRoleResponse;
import com.nhnacademy.accountapi.dto.internal.UserStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    //given
    @BeforeEach
    void setUp(){
        testUser = User.createNormalUser("user1","user@nhn.com","pw1234","홍길동");
        testUser=userRepository.save(testUser);

    }


    @Test
    @DisplayName("1. 회원 저장시 기본 필드(생성시각, ACTIVE 상태 등)검증")
    void saveAndFindById(){

        //when
        User savedUser= userRepository.save(testUser);

        //then
        assertThat (savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getLoginId()).isEqualTo("user1");
        assertThat(savedUser.getEmail()).isEqualTo("user@nhn.com");
        assertThat(savedUser.getPassword()).isEqualTo("pw1234");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.NORMAL);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getCreatedBy()).isNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하는 로그인 ID로 회원 조회 성공")
    void findByLoginId_Success(){
        //when
        Optional<User> foundUserOpt=userRepository.findByLoginId("user1");

        //then
        assertThat(foundUserOpt.isPresent()).isTrue();
        assertThat(foundUserOpt.get().getName()).isEqualTo("홍길동");

    }

    @Test
    @DisplayName("존재하지 않는 로그인 ID로 조회시 비어있는 Optional 반환")
    void findByLoginId_NotFound(){
        //when
        Optional<User> foundUserOpt=userRepository.findByLoginId("ghost"); //저장 되어있지 않은 회원 조회

        //then
        assertThat(foundUserOpt.isEmpty()).isTrue(); //Optional이 완전히 비어있어야함.

    }

    @Test
    @DisplayName("존재하는 로그인 id로 존재 여부 확인 시 true 반환")
    void existsByLoginId_Success(){
        //when
        boolean exists=userRepository.existsByLoginId("user1");

        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 로그인 id로 존재 여부 확인 시 false 반환")
    void existsByLoginId_NotFound(){
        //when
        boolean exists=userRepository.existsByLoginId("ghost");

        //then
        assertThat(exists).isFalse();

    }

    @Test
    @DisplayName("존재하는 이메일로 존재 여부 확인 시 true 반환")
    void existsByEmail_Success(){
        //when
        boolean exists=userRepository.existsByEmail("user@nhn.com");

        //then
        assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("존재하지 않는 이메일로 존재 여부 확인 시 false 반환")
    void existsByEmail_NotFound(){
        //when
        boolean exists=userRepository.existsByEmail("ghost@nhn.com");

        //then
        assertThat(exists).isFalse();

    }

    //실제 db 쿼리가 문법적으로 오류없이 데이터를 잘 조회해 오는지 검증하는것
    @Test
    @DisplayName("")
    void getUsersByCreatedBy_Success(){
        //given -> 999L 오너가 생성한 관리자 2명을 등록함
        Long ownerId=999L;
        User admin1=User.createAdminUser("admin1","pw1234","관리자1",ownerId);
        User admin2=User.createAdminUser("admin2", "pw1234", "관리자2", ownerId);
        userRepository.save(admin1);
        userRepository.save(admin2);

        //when -> 999L 오너 id로 생성한 관리자 목록을 조회함
        List<User> admins= userRepository.getUsersByCreatedBy(ownerId);

        //then -> 2명이 조회되어야 하고, 각각의 아이디가 포함되어있는지 검증
        assertThat(admins.size()).isEqualTo(2);

        // -> admins 리스트에 담긴 User 객체들의 loginId 필드 값을 모았을때, 'admin1'과 'admin2'가 포함되어있는지 검증
        assertThat(admins).extracting(User::getLoginId)
                .containsExactlyInAnyOrder("admin1","admin2");
    }

    @Test
    @DisplayName("생성한 관리자가 없을 때 빈 리스트 반환")
    void getUsersByCreatedBy_Empty(){
        //when -> 생성한 적 없는 888L 오너 id로 조회
        List<User> admins=userRepository.getUsersByCreatedBy(88L);

        //then -> 에러가 나지 않고 비어있는 리스트가 와야함
        assertThat(admins.isEmpty()).isTrue();

    }

    @Test
    @DisplayName("유저 id로 권한(role) 조회 성공")
    void findRoleByUserId_Success(){
        //when
        Optional<UserRoleResponse> roleOpt=userRepository.findRoleByUserId(testUser.getUserId());
        //then
        assertThat(roleOpt).isPresent();
        assertThat((roleOpt).get().userId()).isEqualTo(testUser.getUserId());
        assertThat(roleOpt.get().role()).isEqualTo(UserRole.NORMAL);
    }

    @Test
    @DisplayName("존재 하지 않는 유저 id로 권한 조회 시 비어있는 Optional 반환 ")
    void findRoleByUserId_NotFound(){
        //when
        Optional<UserRoleResponse> roleOpt= userRepository.findRoleByUserId(9999L);

        //then
        assertThat(roleOpt).isEmpty();
    }

    @Test
    @DisplayName("유저 id로 상태 조회 성공")
    void findStatusByUserId_Success(){
        //when
        Optional<UserStatusResponse> statusOpt= userRepository.findStatusByUserId(testUser.getUserId());

        //then
        assertThat(statusOpt).isPresent();
        assertThat(statusOpt.get().userId()).isEqualTo(testUser.getUserId());
        assertThat(statusOpt.get().status()).isEqualTo(UserStatus.ACTIVE);

    }

    @Test
    @DisplayName("존재하지 않는 유저 id로 상태조회 시 비어있는 optional 반환")
    void findStatusByUserId_NotFound(){
        //when
        Optional<UserStatusResponse> statusOpt= userRepository.findStatusByUserId(99999L);

        //then
        assertThat(statusOpt).isEmpty();
    }

    @Test
    @DisplayName("여러 유저id 목록으로 상태 목록 배치 조회 성공")
    void findStatusesByUserIdIn_Success(){
        //given
        User user2= User.createNormalUser("user2","user2@nhn.com","pw5678","김철수");
        userRepository.save(user2);

        List<Long> userIds= List.of(testUser.getUserId(),user2.getUserId());

        //when
        List<UserStatusResponse> statuses= userRepository.findStatusesByUserIdIn(userIds);

        //then
        assertThat(statuses).hasSize(2);
        assertThat(statuses).extracting(UserStatusResponse::userId)
                .containsExactlyInAnyOrder(testUser.getUserId(), user2.getUserId());
    }


    @Test
    @DisplayName("존재하지 않는 유저 ID 목록으로 상태 배치 조회 시 빈 리스트 반환")
    void findStatusesByUserIdIn_NotFound(){
        //when
        List<UserStatusResponse> statuses=userRepository.findStatusesByUserIdIn(List.of(99999L, 88888L));

        //then
        assertThat(statuses).isEmpty();

    }




}