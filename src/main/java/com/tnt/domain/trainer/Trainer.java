package com.tnt.domain.trainer;

import static com.tnt.global.error.model.ErrorMessage.*;
import static io.micrometer.common.util.StringUtils.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.tnt.global.common.entity.BaseTimeEntity;
import com.tnt.global.error.exception.TnTException;

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

	public static final int INVITATION_CODE_LENGTH = 8;

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
	public Trainer(Long id, Long memberId) {
		this.id = id;
		this.memberId = Objects.requireNonNull(memberId, TRAINER_NULL_MEMBER_ID.getMessage());
		setNewInvitationCode();
	}

	public void setNewInvitationCode() {
		StringBuilder sb = new StringBuilder();

		String uuidString = UUID.randomUUID().toString();
		byte[] uuidStringBytes = uuidString.getBytes(StandardCharsets.UTF_8);
		byte[] hashBytes;

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			hashBytes = messageDigest.digest(uuidStringBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new TnTException(TRAINER_INVITATION_CODE_GENERATE_FAILED, e);
		}

		for (int j = 0; j < 4; j++) {
			sb.append(String.format("%02x", hashBytes[j]));
		}

		this.invitationCode = validateInvitationCode(sb.toString().toUpperCase());
	}

	private String validateInvitationCode(String invitationCode) {
		if (isBlank(invitationCode) || invitationCode.length() != INVITATION_CODE_LENGTH) {
			throw new IllegalArgumentException(TRAINER_INVALID_INVITATION_CODE.getMessage());
		}

		return invitationCode;
	}
}
