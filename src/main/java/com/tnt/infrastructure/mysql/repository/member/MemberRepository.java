package com.tnt.infrastructure.mysql.repository.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findBySocialIdAndSocialTypeAndDeletedAtIsNull(String socialId, SocialType socialType);
}
