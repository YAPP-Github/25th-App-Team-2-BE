package com.tnt.domain.trainee;

import static com.tnt.common.error.model.ErrorMessage.*;
import static io.micrometer.common.util.StringUtils.*;
import static java.util.Objects.*;

import java.time.LocalDateTime;

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
@Table(name = "pt_goal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PtGoal extends BaseTimeEntity {

	public static final int CONTENT_LENGTH = 100;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "trainee_id", nullable = false)
	private Long traineeId;

	@Column(name = "content", nullable = false, length = CONTENT_LENGTH)
	private String content;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public PtGoal(Long id, Long traineeId, String content) {
		this.id = id;
		this.traineeId = requireNonNull(traineeId, PT_GOAL_NULL_TRAINEE_ID.getMessage());
		this.content = validateContent(content);
	}

	public void updateDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	private String validateContent(String content) {
		if (isBlank(content) || content.length() > CONTENT_LENGTH) {
			throw new IllegalArgumentException(PT_GOAL_INVALID_CONTENT.getMessage());
		}

		return content;
	}
}
