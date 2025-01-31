package com.tnt.domain.pt;

import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import com.tnt.common.error.model.ErrorMessage;
import com.tnt.domain.trainee.Trainee;
import com.tnt.domain.trainer.Trainer;
import com.tnt.infrastructure.mysql.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private Trainer trainer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private Trainee trainee;

	@Column(name = "started_at", nullable = false)
	private LocalDate startedAt;

	@Column(name = "finished_pt_count", nullable = false)
	private Integer finishedPtCount;

	@Column(name = "total_pt_count", nullable = false)
	private Integer totalPtCount;

	@Column(name = "deleted_at")
	private LocalDate deletedAt;

	@Builder
	public PtTrainerTrainee(Long id, Trainer trainer, Trainee trainee, LocalDate startedAt, Integer finishedPtCount,
		Integer totalPtCount) {
		this.id = id;
		this.trainer = requireNonNull(trainer, ErrorMessage.TRAINER_NULL.getMessage());
		this.trainee = requireNonNull(trainee, ErrorMessage.TRAINEE_NULL.getMessage());
		this.startedAt = requireNonNull(startedAt);
		this.finishedPtCount = requireNonNull(finishedPtCount);
		this.totalPtCount = requireNonNull(totalPtCount);
	}

	public int getCurrentSession() {
		return this.finishedPtCount + 1;
	}
}
