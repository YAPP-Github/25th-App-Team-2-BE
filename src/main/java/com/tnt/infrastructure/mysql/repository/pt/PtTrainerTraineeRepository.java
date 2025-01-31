package com.tnt.infrastructure.mysql.repository.pt;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.pt.PtTrainerTrainee;

public interface PtTrainerTraineeRepository extends JpaRepository<PtTrainerTrainee, Long> {

	boolean existsByTraineeIdAndDeletedAtIsNull(Long traineeId);

	boolean existsByTrainerIdAndTraineeIdAndDeletedAtIsNull(Long trainerId, Long traineeId);
}
