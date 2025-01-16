package com.tnt.domain.trainee;

import static com.tnt.global.error.model.ErrorMessage.PT_GOAL_INVALID_CONTENT;
import static com.tnt.global.error.model.ErrorMessage.PT_GOAL_NULL_TRAINEE_ID;
import static io.micrometer.common.util.StringUtils.isBlank;

import java.util.Objects;

import com.tnt.global.common.entity.BaseTimeEntity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "trainee_id", nullable = false)
	private Long traineeId;

	@Column(name = "content", nullable = false, length = CONTENT_LENGTH)
	private String content;

	@Builder
	public PtGoal(Long id, Long traineeId, String content) {
		this.id = id;
		this.traineeId = Objects.requireNonNull(traineeId, PT_GOAL_NULL_TRAINEE_ID.getMessage());
		this.content = validateContent(content);
	}

	private String validateContent(String content) {
		if (isBlank(content) || content.length() != CONTENT_LENGTH) {
			throw new IllegalArgumentException(PT_GOAL_INVALID_CONTENT.getMessage());
		}

		return content;
	}
}
