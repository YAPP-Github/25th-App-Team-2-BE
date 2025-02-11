package com.tnt.infrastructure.mysql.repository.trainee;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.trainee.Diet;

public interface DietRepository extends JpaRepository<Diet, Integer> {

}
