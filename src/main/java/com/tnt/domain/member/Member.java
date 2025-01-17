package com.tnt.domain.member;

import static com.tnt.global.error.model.ErrorMessage.*;
import static io.micrometer.common.util.StringUtils.isBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.tnt.global.common.entity.BaseTimeEntity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

	private static final int SOCIAL_ID_LENGTH = 50;
	private static final int EMAIL_LENGTH = 100;
	private static final int NAME_LENGTH = 50;
	private static final int PROFILE_IMAGE_URL_LENGTH = 255;
	private static final int SOCIAL_TYPE_LENGTH = 10;

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "social_id", nullable = false, unique = true, length = SOCIAL_ID_LENGTH)
	private String socialId;

	@Column(name = "fcm_token", nullable = false, length = 255)
	private String fcmToken;

	@Column(name = "email", nullable = false, length = EMAIL_LENGTH)
	private String email;

	@Column(name = "name", nullable = false, length = NAME_LENGTH)
	private String name;

	@Column(name = "profile_image_url", nullable = false, length = PROFILE_IMAGE_URL_LENGTH)
	private String profileImageUrl;

	@Column(name = "birthday")
	private LocalDate birthday;

	@Column(name = "service_agreement", nullable = false)
	private boolean serviceAgreement;

	@Column(name = "collection_agreement", nullable = false)
	private boolean collectionAgreement;

	@Column(name = "advertisement_agreement", nullable = false)
	private boolean advertisementAgreement;

	@Column(name = "push_agreement", nullable = false)
	private boolean pushAgreement;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "social_type", nullable = false, length = SOCIAL_TYPE_LENGTH)
	private SocialType socialType;

	@Builder
	public Member(Long id, String socialId, String fcmToken, String email, String name, String profileImageUrl,LocalDate birthday,
		boolean serviceAgreement, boolean collectionAgreement, boolean advertisementAgreement,
		boolean pushAgreement, SocialType socialType) {
		this.id = id;
		this.socialId = validateSocialId(socialId);
		this.fcmToken = fcmToken;
		this.email = validateEmail(email);
		this.name = validateName(name);
		this.profileImageUrl = validateProfileImageUrl(profileImageUrl);
		this.birthday = birthday;
		this.serviceAgreement = serviceAgreement;
		this.collectionAgreement = collectionAgreement;
		this.advertisementAgreement = advertisementAgreement;
		this.pushAgreement = pushAgreement;
		this.socialType = validateSocialType(socialType);
	}

	private String validateSocialId(String socialId) {
		if (isBlank(socialId) || socialId.length() != SOCIAL_ID_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_SOCIAL_ID.getMessage());
		}

		return socialId;
	}

	private String validateEmail(String email) {
		if (isBlank(email) || email.length() != EMAIL_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_EMAIL.getMessage());
		}

		return email;
	}

	private String validateName(String name) {
		if (isBlank(name) || name.length() != NAME_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_NAME.getMessage());
		}

		return name;
	}

	private String validateProfileImageUrl(String profileImageUrl) {
		if (isBlank(profileImageUrl) || profileImageUrl.length() != PROFILE_IMAGE_URL_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_PROFILE_IMAGE_URL.getMessage());
		}

		return profileImageUrl;
	}

	private SocialType validateSocialType(SocialType socialType) {
		if (Objects.isNull(socialType) || socialType.toString().length() != SOCIAL_ID_LENGTH) {
			throw new IllegalArgumentException(MEMBER_INVALID_SOCIAL_TYPE.getMessage());
		}

		return socialType;
	}

	public void updateFcmTokenIfExpired(String fcmToken) {
		if (!this.fcmToken.equals(fcmToken)) {
			this.fcmToken = fcmToken;
		}
	}
}
