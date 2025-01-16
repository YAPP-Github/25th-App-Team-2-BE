package com.tnt.application.s3;

import static com.tnt.global.error.model.ErrorMessage.IMAGE_LOAD_ERROR;
import static com.tnt.global.error.model.ErrorMessage.IMAGE_PROCESSING_ERROR;
import static com.tnt.global.error.model.ErrorMessage.S3_UPLOAD_ERROR;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import net.coobird.thumbnailator.Thumbnails;

import com.tnt.global.error.exception.ImageException;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3Service {

	private static final int MAX_WIDTH = 1200;
	private static final int MAX_HEIGHT = 1200;
	private static final double IMAGE_QUALITY = 0.85;
	private static final String OUTPUT_FORMAT = "jpg";

	private final S3Client s3Client;
	private final WebClient webClient;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	public String uploadFromUrl(String sourceUrl, String folderPath) {
		try {
			byte[] processedImage = downloadAndProcessImage(sourceUrl);

			return uploadToS3(processedImage, folderPath);
		} catch (IOException e) {
			throw new ImageException(IMAGE_PROCESSING_ERROR, e);
		}
	}

	private byte[] downloadAndProcessImage(String sourceUrl) throws IOException {
		byte[] imageBytes = webClient.get()
			.uri(sourceUrl)
			.retrieve()
			.bodyToMono(byte[].class)
			.block();

		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(Objects.requireNonNull(imageBytes)));

		if (Objects.isNull(originalImage)) {
			throw new ImageException(IMAGE_LOAD_ERROR);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Thumbnails.of(originalImage)
			.size(MAX_WIDTH, MAX_HEIGHT)
			.keepAspectRatio(true)
			.outputQuality(IMAGE_QUALITY)
			.outputFormat(OUTPUT_FORMAT)
			.toOutputStream(outputStream);

		return outputStream.toByteArray();
	}

	private String uploadToS3(byte[] imageData, String folderPath) {
		String fileName = UUID.randomUUID() + "." + OUTPUT_FORMAT;
		String s3Key = folderPath + "/" + fileName;

		try {
			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.contentType("image/" + OUTPUT_FORMAT)
				.build();

			s3Client.putObject(request, RequestBody.fromBytes(imageData));

			GetUrlRequest urlRequest = GetUrlRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.build();

			return s3Client.utilities().getUrl(urlRequest).toString();
		} catch (S3Exception e) {
			throw new ImageException(S3_UPLOAD_ERROR, e);
		}
	}
}
