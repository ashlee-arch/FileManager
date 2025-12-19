package com.example.filemanager;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 로직(CustomUserDetailsService)에서 사용
    Optional<Member> findByUsername(String username);

    // 회원가입 중복 체크에서 사용
    boolean existsByUsername(String username);
}
