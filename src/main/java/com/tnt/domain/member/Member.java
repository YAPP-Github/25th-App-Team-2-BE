package com.tnt.domain.member;

import java.time.LocalDateTime;

import com.tnt.global.entity.BaseTimeEntity;

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

	@Column(name = "social_id", nullable = false, unique = true)
	private String socialId;

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "age", nullable = false)
	private int age;

	@Column(name = "profile", nullable = false)
	private String profile;

	@Column(name = "service_agreement", nullable = false)
	private boolean serviceAgreement;

	@Column(name = "collection_agreement", nullable = false)
	private boolean collectionAgreement;

	@Column(name = "push_agreement", nullable = false)
	private boolean pushAgreement;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "social_type", nullable = false)
	private SocialType socialType;

	@Builder
	public Member(Long id, String socialId, String email, String name, int age, SocialType socialType) {
		this.id = id;
		this.socialId = socialId;
		this.email = email;
		this.name = name;
		this.age = age;
		this.profile = "";
		this.serviceAgreement = true;
		this.collectionAgreement = true;
		this.pushAgreement = true;
		this.socialType = socialType;
	}
}
