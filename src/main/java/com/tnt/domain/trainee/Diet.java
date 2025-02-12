package com.tnt.domain.trainee;

import static com.tnt.common.error.model.ErrorMessage.DIET_INVALID_IMAGE_URL;
import static com.tnt.common.error.model.ErrorMessage.DIET_INVALID_MEMO;
import static io.micrometer.common.util.StringUtils.isBlank;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;

import com.tnt.infrastructure.mysql.BaseTimeEntity;

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
@Table(name = "diet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diet extends BaseTimeEntity {

	public static final int DIET_IMAGE_URL_LENGTH = 255;
	public static final int MEMO_LENGTH = 100;
	public static final int DIET_TYPE_LENGTH = 20;

	@Id
	@Tsid
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "trainee_id", nullable = false)
	private Long traineeId;

	@Column(name = "date", nullable = false)
	private LocalDateTime date;

	@Column(name = "diet_image_url", nullable = true, length = DIET_IMAGE_URL_LENGTH)
	private String dietImageUrl;

	@Column(name = "memo", nullable = false, length = MEMO_LENGTH)
	private String memo;

	@Column(name = "deleted_at", nullable = true)
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "diet_type", nullable = false, length = DIET_TYPE_LENGTH)
	private DietType dietType;

	@Builder
	public Diet(Long id, Long traineeId, LocalDateTime date, String dietImageUrl, String memo, DietType dietType) {
		this.id = id;
		this.traineeId = requireNonNull(traineeId);
		this.date = requireNonNull(date);
		this.dietImageUrl = validateDietImageUrl(dietImageUrl);
		this.memo = validateMemo(memo);
		this.dietType = requireNonNull(dietType);
	}

	private String validateDietImageUrl(String dietImageUrl) {
		if (!isBlank(dietImageUrl) && dietImageUrl.length() > DIET_IMAGE_URL_LENGTH) {
			throw new IllegalArgumentException(DIET_INVALID_IMAGE_URL.getMessage());
		}

		return dietImageUrl;
	}

	private String validateMemo(String memo) {
		if (isBlank(memo) || memo.length() > MEMO_LENGTH) {
			throw new IllegalArgumentException(DIET_INVALID_MEMO.getMessage());
		}

		return memo;
	}
}
