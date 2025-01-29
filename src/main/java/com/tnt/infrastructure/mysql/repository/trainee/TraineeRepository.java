package com.tnt.infrastructure.mysql.repository.trainee;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.member.Member;
import com.tnt.domain.trainee.Trainee;

public interface TraineeRepository extends JpaRepository<Trainee, Integer> {

	Optional<Trainee> findByIdAndDeletedAtIsNull(Long id);

	Optional<Trainee> findByMemberAndDeletedAtIsNull(Member member);
}
