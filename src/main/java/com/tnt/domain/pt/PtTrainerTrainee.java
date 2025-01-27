package com.tnt.domain.pt;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import com.tnt.global.error.model.ErrorMessage;
import com.tnt.infrastructure.mysql.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "pt_trainer_trainee")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PtTrainerTrainee extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "trainer_id", nullable = false)
	private Long trainerId;

	@Column(name = "trainee_id", nullable = false)
	private Long traineeId;

	@Column(name = "started_at", nullable = false)
	private LocalDate startedAt;

	@Column(name = "finished_pt_count", nullable = false)
	private Integer finishedPtCount;

	@Column(name = "total_pt_count", nullable = false)
	private Integer totalPtCount;

	@Column(name = "deleted_at")
	private LocalDate deletedAt;

	@Builder
	public PtTrainerTrainee(Long trainerId, Long traineeId, LocalDate startedAt, Integer finishedPtCount,
		Integer totalPtCount) {
		this.trainerId = requireNonNull(trainerId, ErrorMessage.TRAINER_NULL_ID.getMessage());
		this.traineeId = requireNonNull(traineeId, ErrorMessage.TRAINEE_NULL_ID.getMessage());
		this.startedAt = requireNonNull(startedAt);
		this.finishedPtCount = requireNonNull(finishedPtCount);
		this.totalPtCount = requireNonNull(totalPtCount);
	}
}
