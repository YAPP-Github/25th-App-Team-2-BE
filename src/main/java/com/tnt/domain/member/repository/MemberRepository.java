package com.tnt.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tnt.domain.member.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

}
