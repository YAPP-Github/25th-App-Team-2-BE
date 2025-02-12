package com.tnt.domain.pt;

import static com.tnt.common.error.model.ErrorMessage.PT_TRAINER_TRAINEE_NULL;
import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import org.springframework.lang.Nullable;

import com.tnt.infrastructure.mysql.BaseTimeEntity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
@Table(name = "pt_lesson")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PtLesson extends BaseTimeEntity {

	private static final int MEMO_LENGTH = 30;

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pt_trainer_trainee_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private PtTrainerTrainee ptTrainerTrainee;

	@Column(name = "lesson_start", nullable = false)
	private LocalDateTime lessonStart;

	@Column(name = "lesson_end", nullable = false)
	private LocalDateTime lessonEnd;

	@Column(name = "is_completed", nullable = false)
	private Boolean isCompleted;

	@Column(name = "memo", nullable = true, length = MEMO_LENGTH)
	private String memo;

	@Column(name = "session", nullable = false)
	private Integer session;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public PtLesson(Long id, PtTrainerTrainee ptTrainerTrainee, LocalDateTime lessonStart, LocalDateTime lessonEnd,
		@Nullable String memo, Integer session) {
		this.id = id;
		this.ptTrainerTrainee = requireNonNull(ptTrainerTrainee, PT_TRAINER_TRAINEE_NULL.getMessage());
		this.lessonStart = requireNonNull(lessonStart);
		this.lessonEnd = requireNonNull(lessonEnd);
		this.isCompleted = false;
		this.session = requireNonNull(session);
		validateAndSetMemo(memo);
	}

	public void completeLesson() {
		this.isCompleted = true;
	}

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
	}

	public void increaseSession() {
		this.session++;
	}

	private void validateAndSetMemo(String memo) {
		if (isNull(memo)) {
			return;
		}

		if (memo.length() > MEMO_LENGTH) {
			throw new IllegalArgumentException("메모는 30자 이하여야 합니다.");
		}

		this.memo = memo;
	}
}
