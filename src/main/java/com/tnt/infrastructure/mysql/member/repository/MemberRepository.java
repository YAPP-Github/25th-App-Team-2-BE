package com.tnt.infrastructure.mysql.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tnt.domain.member.Member;
import com.tnt.domain.member.SocialType;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findBySocialIdAndSocialType(String socialId, SocialType socialType);
}
