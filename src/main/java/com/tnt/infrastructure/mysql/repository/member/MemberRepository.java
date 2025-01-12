package com.tnt.infrastructure.mysql.repository.member;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findBySocialIdAndSocialTypeAndDeletedAt(String socialId, SocialType socialType,
		LocalDateTime deletedAt);
}
