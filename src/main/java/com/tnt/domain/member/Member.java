package com.tnt.domain.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "social_id", nullable = false, unique = true, length = 50)
	private String socialId;

	@Column(name = "fcm_token", nullable = false, length = 255)
	private String fcmToken;

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "birthday", nullable = false)
	private LocalDate birthday;

	@Column(name = "profile_image_url", nullable = false, length = 255)
	private String profileImageUrl;

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
	@Column(name = "social_type", nullable = false, length = 30)
	private SocialType socialType;

	@Builder
	public Member(Long id, String socialId, String fcmToken, String email, String name, LocalDate birthday,
		String profileImageUrl, boolean serviceAgreement, boolean collectionAgreement, boolean advertisementAgreement,
		boolean pushAgreement, SocialType socialType) {
		this.id = id;
		this.socialId = socialId;
		this.fcmToken = fcmToken;
		this.email = email;
		this.name = name;
		this.birthday = birthday;
		this.profileImageUrl = profileImageUrl;
		this.serviceAgreement = serviceAgreement;
		this.collectionAgreement = collectionAgreement;
		this.advertisementAgreement = advertisementAgreement;
		this.pushAgreement = pushAgreement;
		this.socialType = socialType;
	}

	public void updateFcmTokenIfExpired(String fcmToken) {
		if (!this.fcmToken.equals(fcmToken)) {
			this.fcmToken = fcmToken;
		}
	}
}
