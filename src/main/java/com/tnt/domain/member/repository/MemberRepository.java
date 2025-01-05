package com.tnt.domain.member.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByIdAndDeletedAt(Long id, LocalDateTime deletedAt);
}
