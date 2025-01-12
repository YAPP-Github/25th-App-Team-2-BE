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

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "birthday", nullable = false)
	private LocalDate birthday;

	@Column(name = "profile_image_url", nullable = false)
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

	private Member(Builder builder) {
		this.id = builder.id;
		this.socialId = builder.socialId;
		this.email = builder.email;
		this.name = builder.name;
		this.birthday = builder.birthday;
		this.profileImageUrl = builder.profileImageUrl;
		this.serviceAgreement = builder.serviceAgreement;
		this.collectionAgreement = builder.collectionAgreement;
		this.advertisementAgreement = builder.advertisementAgreement;
		this.pushAgreement = builder.pushAgreement;
		this.socialType = builder.socialType;
	}

	public static class Builder {

		private Long id;
		private String socialId;
		private String email;
		private String name;
		private LocalDate birthday;
		private String profileImageUrl = "";
		private boolean serviceAgreement = true;
		private boolean collectionAgreement = true;
		private boolean advertisementAgreement = true;
		private boolean pushAgreement = true;
		private SocialType socialType;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder socialId(String socialId) {
			this.socialId = socialId;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder birthday(LocalDate birthday) {
			this.birthday = birthday;
			return this;
		}

		public Builder profileImageUrl(String profileImageUrl) {
			this.profileImageUrl = profileImageUrl;
			return this;
		}

		public Builder serviceAgreement(boolean serviceAgreement) {
			this.serviceAgreement = serviceAgreement;
			return this;
		}

		public Builder collectionAgreement(boolean collectionAgreement) {
			this.collectionAgreement = collectionAgreement;
			return this;
		}

		public Builder advertisementAgreement(boolean advertisementAgreement) {
			this.advertisementAgreement = advertisementAgreement;
			return this;
		}

		public Builder pushAgreement(boolean pushAgreement) {
			this.pushAgreement = pushAgreement;
			return this;
		}

		public Builder socialType(SocialType socialType) {
			this.socialType = socialType;
			return this;
		}

		public Member build() {
			return new Member(this);
		}
	}
}
