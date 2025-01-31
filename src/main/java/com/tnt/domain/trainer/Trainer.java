package com.tnt.domain.trainer;

import static com.tnt.common.error.model.ErrorMessage.TRAINER_INVALID_INVITATION_CODE;
import static com.tnt.common.error.model.ErrorMessage.TRAINER_INVITATION_CODE_GENERATE_FAILED;
import static com.tnt.common.error.model.ErrorMessage.TRAINER_NULL_MEMBER;
import static io.micrometer.common.util.StringUtils.isBlank;
import static jakarta.persistence.ConstraintMode.NO_CONSTRAINT;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

import com.tnt.common.error.exception.TnTException;
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
@Table(name = "trainer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trainer extends BaseTimeEntity {

	public static final int INVITATION_CODE_LENGTH = 8;

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(NO_CONSTRAINT))
	private Member member;

	@Column(name = "invitation_code", nullable = false, length = INVITATION_CODE_LENGTH)
	private String invitationCode;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	public Trainer(Long id, Member member) {
		this.id = id;
		this.member = requireNonNull(member, TRAINER_NULL_MEMBER.getMessage());
		setNewInvitationCode();
	}

	public void setNewInvitationCode() {
		byte[] hashBytes;
		StringBuilder sb = new StringBuilder();

		String uuidString = UUID.randomUUID().toString();
		byte[] uuidStringBytes = uuidString.getBytes(StandardCharsets.UTF_8);

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

	public void updateDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}
}
