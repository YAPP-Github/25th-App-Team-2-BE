package com.tnt.application.s3;

import static com.tnt.common.constant.ImageConstant.TRAINEE_DEFAULT_IMAGE;
import static com.tnt.common.constant.ImageConstant.TRAINEE_S3_PROFILE_IMAGE_PATH;
import static com.tnt.common.constant.ImageConstant.TRAINER_DEFAULT_IMAGE;
import static com.tnt.common.constant.ImageConstant.TRAINER_S3_PROFILE_IMAGE_PATH;
import static com.tnt.common.error.model.ErrorMessage.IMAGE_NOT_FOUND;
import static com.tnt.common.error.model.ErrorMessage.IMAGE_NOT_SUPPORT;
import static com.tnt.common.error.model.ErrorMessage.UNSUPPORTED_MEMBER_TYPE;
import static java.util.Objects.isNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.stream.ImageInputStream;

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
				folderPath = TRAINER_S3_PROFILE_IMAGE_PATH;
			}
			case TRAINEE -> {
				defaultImage = TRAINEE_DEFAULT_IMAGE;
				folderPath = TRAINEE_S3_PROFILE_IMAGE_PATH;
			}
			default -> throw new IllegalArgumentException(UNSUPPORTED_MEMBER_TYPE.getMessage());
		}

		return uploadImage(defaultImage, folderPath, profileImage);
	}

	public String uploadImage(String defaultImage, String folderPath, @Nullable MultipartFile image) {
		if (isNull(image)) {
			return defaultImage;
		}

		String extension = validateImageFormat(image);

		try {
			byte[] processedImage = processImage(image, extension);

			return s3Adapter.uploadFile(processedImage, folderPath, extension);
		} catch (Exception e) {
			return defaultImage;
		}
	}

	public void deleteProfileImage(String imageUrl) {
		if (imageUrl.equals(TRAINER_DEFAULT_IMAGE) || imageUrl.equals(TRAINEE_DEFAULT_IMAGE)) {
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

		// EXIF 정보를 포함하여 이미지 읽기
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(extension);
		ImageReader reader = readers.next();

		ImageInputStream iis = ImageIO.createImageInputStream(image.getInputStream());
		reader.setInput(iis, true);

		// 이미지 메타데이터 읽기
		IIOMetadata metadata = reader.getImageMetadata(0);
		TIFFDirectory dir = TIFFDirectory.createFromMetadata(metadata);
		TIFFField orientation = dir.getTIFFField(274); // EXIF Orientation 태그

		// 원본 이미지 읽기
		BufferedImage originalImage = reader.read(0);

		// 방향에 따라 이미지 회전
		if (orientation != null) {
			int orientationValue = orientation.getAsInt(0);
			originalImage = rotateImageByOrientation(originalImage, orientationValue);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Thumbnails.of(originalImage)
			.size(MAX_WIDTH, MAX_HEIGHT)
			.keepAspectRatio(true)
			.outputQuality(IMAGE_QUALITY)
			.outputFormat(extension)
			.toOutputStream(outputStream);

		return outputStream.toByteArray();
	}

	private BufferedImage rotateImageByOrientation(BufferedImage image, int orientation) throws IOException {
		return switch (orientation) {
			case 3 -> // 180도 회전
				Thumbnails.of(image).scale(1.0).rotate(180).asBufferedImage();
			case 6 -> // 90도 시계방향
				Thumbnails.of(image).scale(1.0).rotate(90).asBufferedImage();
			case 8 -> // 270도 시계방향
				Thumbnails.of(image).scale(1.0).rotate(270).asBufferedImage();
			default -> image;
		};
	}
}
