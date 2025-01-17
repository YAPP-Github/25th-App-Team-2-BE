package com.tnt.domain.trainee;

import static com.tnt.global.error.model.ErrorMessage.TRAINEE_INVALID_CAUTION_NOTE;
import static com.tnt.global.error.model.ErrorMessage.TRAINEE_NULL_MEMBER_ID;
import static io.micrometer.common.util.StringUtils.isBlank;

import java.time.LocalDateTime;
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
@Table(name = "trainee")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trainee extends BaseTimeEntity {

	public static final int CAUTION_NOTE_LENGTH = 100;

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "height", nullable = false)
	private double height;

	@Column(name = "weight", nullable = false)
	private double weight;

	@Column(name = "caution_note", nullable = false, length = CAUTION_NOTE_LENGTH)
	private String cautionNote;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	public Trainee(Long id, Long memberId, double height, double weight, String cautionNote) {
		this.id = id;
		this.memberId = Objects.requireNonNull(memberId, TRAINEE_NULL_MEMBER_ID.getMessage());
		this.height = height;
		this.weight = weight;
		this.cautionNote = validateCautionNote(cautionNote);
	}

	private String validateCautionNote(String cautionNote) {
		if (isBlank(cautionNote) || cautionNote.length() > CAUTION_NOTE_LENGTH) {
			throw new IllegalArgumentException(TRAINEE_INVALID_CAUTION_NOTE.getMessage());
		}

		return cautionNote;
	}
}
