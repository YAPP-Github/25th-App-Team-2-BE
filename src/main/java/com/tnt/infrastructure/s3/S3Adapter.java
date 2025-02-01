package com.tnt.infrastructure.s3;

import static com.tnt.common.error.model.ErrorMessage.S3_DELETE_ERROR;
import static com.tnt.common.error.model.ErrorMessage.S3_UPLOAD_ERROR;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tnt.common.error.exception.ImageException;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3Adapter {

	public static final String IMAGE_BASE_URL = "https://images.tntapp.co.kr/";

	private final S3Client s3Client;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	public String uploadFile(byte[] fileData, String folderPath, String extension) {
		String fileName = TSID.Factory.getTsid() + "." + extension;
		String s3Key = folderPath + "/" + fileName;

		try {
			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.contentType("image/" + extension)
				.build();

			s3Client.putObject(request, RequestBody.fromBytes(fileData));

			return IMAGE_BASE_URL + s3Key;
		} catch (S3Exception e) {
			throw new ImageException(S3_UPLOAD_ERROR, e);
		}
	}

	public void deleteFile(String s3Key) {
		try {
			DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.build();

			s3Client.deleteObject(request);
		} catch (S3Exception e) {
			throw new ImageException(S3_DELETE_ERROR, e);
		}
	}
}
