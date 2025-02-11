package com.tnt.application.s3;

import static com.tnt.common.constant.ProfileConstant.TRAINEE_DEFAULT_IMAGE;
import static com.tnt.common.constant.ProfileConstant.TRAINEE_S3_PROFILE_PATH;
import static com.tnt.common.constant.ProfileConstant.TRAINER_DEFAULT_IMAGE;
import static com.tnt.common.constant.ProfileConstant.TRAINER_S3_PROFILE_PATH;
import static com.tnt.common.error.model.ErrorMessage.IMAGE_NOT_FOUND;
import static com.tnt.common.error.model.ErrorMessage.IMAGE_NOT_SUPPORT;
import static com.tnt.common.error.model.ErrorMessage.UNSUPPORTED_MEMBER_TYPE;
import static java.util.Objects.isNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

import com.tnt.common.error.exception.ImageException;
import com.tnt.domain.member.MemberType;
import com.tnt.infrastructure.s3.S3Adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "svg");
	private static final int MAX_WIDTH = 1200;
	private static final int MAX_HEIGHT = 1200;
	private static final double IMAGE_QUALITY = 0.85;

	private final S3Adapter s3Adapter;

	public String uploadProfileImage(@Nullable MultipartFile profileImage, MemberType memberType) {
		String defaultImage;
		String folderPath;

		switch (memberType) {
			case TRAINER -> {
				defaultImage = TRAINER_DEFAULT_IMAGE;
				folderPath = TRAINER_S3_PROFILE_PATH;
			}
			case TRAINEE -> {
				defaultImage = TRAINEE_DEFAULT_IMAGE;
				folderPath = TRAINEE_S3_PROFILE_PATH;
			}
			default -> throw new IllegalArgumentException(UNSUPPORTED_MEMBER_TYPE.getMessage());
		}

		if (isNull(profileImage)) {
			return defaultImage;
		}

		String extension = validateImageFormat(profileImage);

		try {
			byte[] processedImage = processImage(profileImage, extension);

			return s3Adapter.uploadFile(processedImage, folderPath, extension);
		} catch (Exception e) {
			return defaultImage;
		}
	}

	public void deleteProfileImage(String imageUrl) {
		if (isDefaultImage(imageUrl)) {
			return;
		}

		try {
			String s3Key = imageUrl.replace(S3Adapter.IMAGE_BASE_URL, "");

			s3Adapter.deleteFile(s3Key);
		} catch (Exception e) {
			// S3 삭제 실패해도 회원 탈퇴는 진행되어야 하므로 로그만 남김
			log.error("이미지 삭제 실패: {}", imageUrl, e);
		}
	}

	private String validateImageFormat(MultipartFile image) {
		String originalFilename = image.getOriginalFilename();

		if (isNull(originalFilename)) {
			throw new ImageException(IMAGE_NOT_FOUND);
		}

		String extension = getExtension(originalFilename).toLowerCase();

		if (!SUPPORTED_FORMATS.contains(extension)) {
			throw new ImageException(IMAGE_NOT_SUPPORT);
		}

		return extension;
	}

	private String getExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');

		if (lastDotIndex == -1) {
			throw new ImageException(IMAGE_NOT_FOUND);
		}

		return filename.substring(lastDotIndex + 1);
	}

	private byte[] processImage(MultipartFile image, String extension) throws IOException {
		if ("svg".equals(extension)) {
			return image.getBytes();
		}

		BufferedImage originalImage = ImageIO.read(image.getInputStream());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Thumbnails.of(originalImage)
			.size(MAX_WIDTH, MAX_HEIGHT)
			.keepAspectRatio(true)
			.outputQuality(IMAGE_QUALITY)
			.useExifOrientation(true)
			.outputFormat(extension)
			.toOutputStream(outputStream);

		return outputStream.toByteArray();
	}

	private boolean isDefaultImage(String imageUrl) {
		return imageUrl.equals(TRAINER_DEFAULT_IMAGE) || imageUrl.equals(TRAINEE_DEFAULT_IMAGE);
	}
}
