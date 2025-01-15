package com.tnt.domain.trainer;

import static com.tnt.global.error.model.ErrorMessage.TRAINER_INVALID_INVITATION_CODE;
import static com.tnt.global.error.model.ErrorMessage.TRAINER_NULL_ID;
import static com.tnt.global.error.model.ErrorMessage.TRAINER_NULL_MEMBER_ID;
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
@Table(name = "trainer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trainer extends BaseTimeEntity {

	private static final int INVITATION_CODE_LENGTH = 8;

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "invitation_code", nullable = false, length = INVITATION_CODE_LENGTH)
	private String invitationCode;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	public Trainer(Long id, Long memberId, String invitationCode) {
		this.id = Objects.requireNonNull(id, TRAINER_NULL_ID.getMessage());
		this.memberId = Objects.requireNonNull(memberId, TRAINER_NULL_MEMBER_ID.getMessage());
		this.invitationCode = validateInvitationCode(invitationCode);
	}

	private String validateInvitationCode(String invitationCode) {
		if (isBlank(invitationCode) || invitationCode.length() != INVITATION_CODE_LENGTH) {
			throw new IllegalArgumentException(TRAINER_INVALID_INVITATION_CODE.getMessage());
		}

		return invitationCode;
	}
}
