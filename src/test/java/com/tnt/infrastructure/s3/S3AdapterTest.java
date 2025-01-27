package com.tnt.infrastructure.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.tnt.common.error.exception.ImageException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class S3AdapterTest {

	@InjectMocks
	private S3Adapter s3Adapter;

	@Mock
	private S3Client s3Client;

	@Mock
	private S3Utilities s3Utilities;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(s3Adapter, "bucketName", "test-bucket");
	}

	@Test
	@DisplayName("파일 업로드 성공")
	void upload_file_success() throws Exception {
		// given
		byte[] fileData = "test data".getBytes();
		String folderPath = "test/folder";
		String extension = "jpg";

		// when
		String result = s3Adapter.uploadFile(fileData, folderPath, extension);

		// then
		assertThat(result).startsWith("https://images.tntapp.co.kr/test/folder/");
		verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
	}

	@Test
	@DisplayName("S3 업로드 실패")
	void upload_file_S3_error() {
		// given
		byte[] fileData = "test data".getBytes();
		String folderPath = "test/folder";
		String extension = "jpg";

		given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).willThrow(
			S3Exception.builder().build());

		// when & then
		assertThrows(ImageException.class, () -> s3Adapter.uploadFile(fileData, folderPath, extension));
	}
}
