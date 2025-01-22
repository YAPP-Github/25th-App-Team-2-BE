package com.tnt.infrastructure.mysql.repository.trainee;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.trainee.PtGoal;

public interface PtGoalRepository extends JpaRepository<PtGoal, Integer> {

}
