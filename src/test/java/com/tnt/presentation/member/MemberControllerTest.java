package com.tnt.presentation.member;

import static com.tnt.domain.constant.Constant.TRAINEE;
import static com.tnt.domain.constant.Constant.TRAINEE_DEFAULT_IMAGE;
import static com.tnt.domain.constant.Constant.TRAINER;
import static com.tnt.domain.constant.Constant.TRAINER_DEFAULT_IMAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.infrastructure.redis.AbstractContainerBaseTest;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest extends AbstractContainerBaseTest {

	private final MockMultipartFile profileImage = new MockMultipartFile("profileImage", "test.jpg",
		IMAGE_JPEG_VALUE, "test image content".getBytes());

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("통합 테스트 - 트레이너 회원가입 성공")
	void sign_up_trainer_success() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("fcm-token-test", TRAINER, "KAKAO", "12345", "test@kakao.com", true,
			true, true, true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/members/sign-up")
			.file(jsonRequest)
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberType").value(TRAINER))
			.andExpect(jsonPath("$.sessionId").exists())
			.andExpect(jsonPath("$.name").value("홍길동"))
			.andExpect(jsonPath("$.profileImageUrl").value(TRAINER_DEFAULT_IMAGE));
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 회원가입 성공")
	void sign_up_trainee_success() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("fcm-token-test", TRAINEE, "KAKAO", "12345", "test@kakao.com", true,
			true, true, true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/members/sign-up")
			.file(jsonRequest)
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberType").value(TRAINEE))
			.andExpect(jsonPath("$.sessionId").exists())
			.andExpect(jsonPath("$.name").value("홍길동"))
			.andExpect(jsonPath("$.profileImageUrl").value(TRAINEE_DEFAULT_IMAGE));
	}

	@Test
	@DisplayName("통합 테스트 - 잘못된 회원 타입으로 회원가입 실패")
	void sign_up_invalid_member_type_fail() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("fcm-token-test", "invalid_type", "KAKAO", "12345", "test@kakao.com",
			true,
			true, true, true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/members/sign-up")
			.file(jsonRequest)
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("통합 테스트 - 필수 필드 누락으로 회원가입 실패")
	void sign_up_missing_required_field_fail() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("", TRAINER, "KAKAO", "12345", "test@kakao.com", true,
			true, true, true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));

		// when
		var jsonRequest = new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
		var result = mockMvc.perform(multipart("/members/sign-up")
			.file(jsonRequest)
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));

		// then
		result.andExpect(status().is4xxClientError());
	}
}
