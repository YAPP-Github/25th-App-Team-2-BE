package com.tnt.infrastructure.mysql.repository.trainee;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tnt.domain.trainee.Diet;

public interface DietRepository extends JpaRepository<Diet, Long> {

	Optional<Diet> findByIdAndTraineeIdAndDeletedAtIsNull(Long id, Long traineeId);

	List<Diet> findAllByTraineeIdAndDeletedAtIsNull(Long traineeId);

	boolean existsByTraineeIdAndDateAndDeletedAtIsNull(Long traineeId, LocalDateTime date);
}
