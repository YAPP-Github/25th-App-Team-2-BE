package com.tnt.infrastructure.mysql.repository.trainee;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.trainee.Trainee;

public interface TraineeRepository extends JpaRepository<Trainee, Integer> {

}
