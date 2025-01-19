package com.tnt.application.s3;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.tnt.global.error.exception.ImageException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

	@InjectMocks
	private S3Service s3Service;

	@Mock
	private S3Client s3Client;

	@Mock
	private S3Utilities s3Utilities;

	private byte[] createDummyImageData() throws IOException {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);

		return baos.toByteArray();
	}

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
	}

	@Test
	@DisplayName("이미지 업로드 성공 - JPG")
	void uploadFile_success_jpg() throws Exception {
		// given
		String folderPath = "test/folder";
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
			createDummyImageData());
		URI mockUri = URI.create("https://test-bucket.s3.amazonaws.com/test/folder/123.jpg");

		given(s3Client.utilities()).willReturn(s3Utilities);
		given(s3Utilities.getUrl(any(GetUrlRequest.class))).willReturn(mockUri.toURL());
		given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).willReturn(null);

		// when
		String result = s3Service.uploadFile(image, folderPath);

		// then
		assertThat(result).startsWith("https://test-bucket.s3.amazonaws.com/test/folder/").endsWith(".jpg");
	}

	@Test
	@DisplayName("이미지 업로드 성공 - PNG")
	void uploadFile_success_png() throws Exception {
		// given
		String folderPath = "test/folder";
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
			createDummyImageData());
		URI mockUri = URI.create("https://test-bucket.s3.amazonaws.com/test/folder/123.png");

		given(s3Client.utilities()).willReturn(s3Utilities);
		given(s3Utilities.getUrl(any(GetUrlRequest.class))).willReturn(mockUri.toURL());
		given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).willReturn(null);

		// when
		String result = s3Service.uploadFile(image, folderPath);

		// then
		assertThat(result).startsWith("https://test-bucket.s3.amazonaws.com/test/folder/").endsWith(".png");
	}

	@Test
	@DisplayName("지원하지 않는 이미지 형식으로 업로드 실패")
	void uploadFile_fail_unsupported_format() throws IOException {
		// given
		String folderPath = "test/folder";
		MockMultipartFile image = new MockMultipartFile("image", "test.gif", "image/gif", createDummyImageData());

		// when & then
		assertThrows(ImageException.class, () -> s3Service.uploadFile(image, folderPath));
	}

	@Test
	@DisplayName("파일명이 없는 경우 업로드 실패")
	void uploadFile_fail_no_filename() throws IOException {
		// given
		String folderPath = "test/folder";
		MockMultipartFile image = new MockMultipartFile("image", "", MediaType.IMAGE_JPEG_VALUE,
			createDummyImageData());

		// when & then
		assertThrows(ImageException.class, () -> s3Service.uploadFile(image, folderPath));
	}

	@Test
	@DisplayName("S3 업로드 실패")
	void uploadFile_fail_s3_error() throws Exception {
		// given
		String folderPath = "test/folder";
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
			createDummyImageData());

		given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
			.willThrow(S3Exception.builder().build());

		// when & then
		assertThrows(ImageException.class, () -> s3Service.uploadFile(image, folderPath));
	}

	@Test
	@DisplayName("이미지 처리 실패")
	void uploadFile_fail_image_processing() {
		// given
		String folderPath = "test/folder";
		MockMultipartFile image = new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
			"invalid image data".getBytes());

		// when & then
		assertThrows(ImageException.class, () -> s3Service.uploadFile(image, folderPath));
	}
}
