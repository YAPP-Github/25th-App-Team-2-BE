package com.tnt.application.s3;

import static com.tnt.common.constant.ImageConstant.TRAINEE_DEFAULT_IMAGE;
import static com.tnt.domain.member.MemberType.TRAINEE;
import static com.tnt.domain.member.MemberType.TRAINER;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.TIFF_TAG_ORIENTATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.tnt.common.error.exception.ImageException;
import com.tnt.infrastructure.s3.S3Adapter;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

	@InjectMocks
	private S3Service s3Service;

	@Mock
	private S3Adapter s3Adapter;

	private BufferedImage createTestImage() {
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 100, 50);
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 50, 100, 50);
		graphics.dispose();

		return image;
	}

	private byte[] createDummyImageData(int orientation) throws IOException {
		BufferedImage image = createTestImage();

		// 생성된 이미지를 JPG 형식의 바이트 배열로 변환
		ByteArrayOutputStream initialBaos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", initialBaos);
		byte[] imageBytes = initialBaos.toByteArray();

		// EXIF orientation 추가
		ByteArrayOutputStream finalBaos = new ByteArrayOutputStream();
		TiffOutputSet outputSet = new TiffOutputSet();
		TiffOutputDirectory rootDirectory = outputSet.getOrCreateRootDirectory();

		// 3 = 180도 회전
		rootDirectory.add(TIFF_TAG_ORIENTATION, (short)orientation);

		// 기존 이미지에 EXIF 메타데이터를 추가하여 새로운 이미지 생성
		new ExifRewriter().updateExifMetadataLossless(imageBytes, finalBaos, outputSet);

		return finalBaos.toByteArray();
	}

	@Test
	@DisplayName("트레이너 프로필 이미지 업로드 성공")
	void upload_trainer_profile_image_success() throws Exception {
		// given
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", IMAGE_JPEG_VALUE, createDummyImageData(1));
		String expectedUrl = "https://bucket.s3.amazonaws.com/trainer/profile/123.jpg";

		given(s3Adapter.uploadFile(any(byte[].class), anyString(), anyString())).willReturn(expectedUrl);

		// when
		String result = s3Service.uploadProfileImage(image, TRAINER);

		// then
		assertThat(result).isEqualTo(expectedUrl);
	}

	@Test
	@DisplayName("트레이니 프로필 이미지가 null일 경우 기본 이미지 반환 성공")
	void return_trainee_default_image_success() {
		// when
		String result = s3Service.uploadProfileImage(null, TRAINEE);

		// then
		assertThat(result).isEqualTo(TRAINEE_DEFAULT_IMAGE);
	}

	@Test
	@DisplayName("지원하지 않는 이미지 형식으로 업로드 실패")
	void upload_profile_image_unsupported_format_error() throws IOException {
		// given
		MockMultipartFile image = new MockMultipartFile("image", "test.gif", "image/gif", createDummyImageData(1));

		// when & then
		assertThrows(ImageException.class, () -> s3Service.uploadProfileImage(image, TRAINER));
	}

	@Test
	@DisplayName("orientation이 3일 때 이미지 180도 회전 성공")
	void rotate_image_orientation_3_success() throws IOException {
		// given
		BufferedImage originalImage = createTestImage();
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", IMAGE_JPEG_VALUE, createDummyImageData(3));

		// when
		BufferedImage rotatedImage = ReflectionTestUtils.invokeMethod(s3Service, "rotateImageIfRequired", originalImage,
			image);

		// then
		assertThat(requireNonNull(rotatedImage).getRGB(50, 25)).isEqualTo(Color.BLACK.getRGB());
		assertThat(requireNonNull(rotatedImage).getRGB(50, 75)).isEqualTo(Color.WHITE.getRGB());
	}

	@Test
	@DisplayName("orientation이 6일 때 이미지 90도 회전 성공")
	void rotate_image_orientation_6_success() throws IOException {
		// given
		BufferedImage originalImage = createTestImage();
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", IMAGE_JPEG_VALUE, createDummyImageData(6));

		// when
		BufferedImage rotatedImage = ReflectionTestUtils.invokeMethod(s3Service, "rotateImageIfRequired", originalImage,
			image);

		// then
		assertThat(requireNonNull(rotatedImage).getRGB(25, 50)).isEqualTo(Color.BLACK.getRGB());
		assertThat(requireNonNull(rotatedImage).getRGB(75, 50)).isEqualTo(Color.WHITE.getRGB());
	}
}
