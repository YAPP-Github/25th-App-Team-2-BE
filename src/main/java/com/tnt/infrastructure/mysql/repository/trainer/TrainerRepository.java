package com.tnt.infrastructure.mysql.repository.trainer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.member.Member;
import com.tnt.domain.trainer.Trainer;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

	Optional<Trainer> findByMemberIdAndDeletedAtIsNull(Long memberId);

	Optional<Trainer> findByInvitationCodeAndDeletedAtIsNull(String invitationCode);

	Optional<Trainer> findByMemberAndDeletedAtIsNull(Member memberId);
}
