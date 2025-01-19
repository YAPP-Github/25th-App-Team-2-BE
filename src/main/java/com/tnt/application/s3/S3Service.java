package com.tnt.application.s3;

import static com.tnt.global.error.model.ErrorMessage.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

import com.tnt.global.error.exception.ImageException;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3Service {

	private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "svg");
	private static final int MAX_WIDTH = 1200;
	private static final int MAX_HEIGHT = 1200;
	private static final double IMAGE_QUALITY = 0.85;

	private final S3Client s3Client;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	public String uploadFile(MultipartFile image, String folderPath) {
		validateImageFormat(image);

		String extension = getExtension(Objects.requireNonNull(image.getOriginalFilename())).toLowerCase();

		try {
			byte[] processedImage = processImage(image, extension);

			return uploadToS3(processedImage, folderPath, extension);
		} catch (IOException e) {
			throw new ImageException(IMAGE_PROCESSING_ERROR, e);
		}
	}

	private void validateImageFormat(MultipartFile image) {
		String originalFilename = image.getOriginalFilename();

		if (originalFilename == null) {
			throw new ImageException(IMAGE_NOT_FOUND);
		}

		String extension = getExtension(originalFilename).toLowerCase();

		if (!SUPPORTED_FORMATS.contains(extension)) {
			throw new ImageException(IMAGE_NOT_SUPPORT);
		}
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

		if (Objects.isNull(originalImage)) {
			throw new ImageException(IMAGE_LOAD_ERROR);
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

	private String uploadToS3(byte[] imageData, String folderPath, String extension) {
		String fileName = TSID.Factory.getTsid() + "." + extension;
		String s3Key = folderPath + "/" + fileName;

		try {
			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(s3Key)
				.contentType("image/" + extension)
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
