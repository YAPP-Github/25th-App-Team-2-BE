package com.tnt.domain.trainee;

import static com.tnt.common.error.model.ErrorMessage.TRAINEE_INVALID_CAUTION_NOTE;
import static com.tnt.common.error.model.ErrorMessage.TRAINEE_NULL_HEIGHT;
import static com.tnt.common.error.model.ErrorMessage.TRAINEE_NULL_MEMBER;
import static com.tnt.common.error.model.ErrorMessage.TRAINEE_NULL_WEIGHT;
import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import org.springframework.lang.Nullable;

import com.tnt.domain.member.Member;
import com.tnt.infrastructure.mysql.BaseTimeEntity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private Member member;

	@Column(name = "height", nullable = false)
	private Double height;

	@Column(name = "weight", nullable = false)
	private Double weight;

	@Column(name = "caution_note", nullable = true, length = CAUTION_NOTE_LENGTH)
	private String cautionNote;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Builder
	public Trainee(Long id, Member member, Double height, Double weight, @Nullable String cautionNote) {
		this.id = id;
		this.member = requireNonNull(member, TRAINEE_NULL_MEMBER.getMessage());
		this.height = requireNonNull(height, TRAINEE_NULL_HEIGHT.getMessage());
		this.weight = requireNonNull(weight, TRAINEE_NULL_WEIGHT.getMessage());
		validateAndSetCautionNote(cautionNote);
	}

	private void validateAndSetCautionNote(String cautionNote) {
		if (isNull(cautionNote)) {
			return;
		}

		if (cautionNote.length() > CAUTION_NOTE_LENGTH) {
			throw new IllegalArgumentException(TRAINEE_INVALID_CAUTION_NOTE.getMessage());
		}

		this.cautionNote = cautionNote;
	}

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
	}
}
