package com.tnt.infrastructure.mysql.repository.pt;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.trainee.PtGoal;

public interface PtGoalRepository extends JpaRepository<PtGoal, Integer> {

	List<PtGoal> findAllByTraineeId(Long traineeId);
}
