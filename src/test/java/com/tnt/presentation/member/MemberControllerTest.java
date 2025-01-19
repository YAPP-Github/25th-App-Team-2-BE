package com.tnt.presentation.member;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.dto.member.request.SignUpRequest;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@AfterEach
	void tearDown() {
		Set<String> keys = redisTemplate.keys("*");
		if (!keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}

	private MockMultipartFile createProfileImage() {
		return new MockMultipartFile("profileImage", "test.jpg", IMAGE_JPEG_VALUE, "test image content".getBytes());
	}

	private MockMultipartFile createSignUpRequest(SignUpRequest request) throws
		JsonProcessingException {
		return new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
			objectMapper.writeValueAsString(request).getBytes());
	}

	private SignUpRequest createDefaultSignUpRequest(String memberType) {
		return new SignUpRequest("fcm-token-test", memberType, "KAKAO", "12345", "test@kakao.com", "홍길동",
			LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));
	}

	private ResultActions performSignUpRequest(SignUpRequest request, MockMultipartFile profileImage) throws Exception {
		return mockMvc.perform(multipart("/member/sign-up")
			.file(createSignUpRequest(request))
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 회원가입 성공")
	void sign_up_trainer_success() throws Exception {
		// given
		SignUpRequest request = createDefaultSignUpRequest("trainer");

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 회원가입 성공")
	void sign_up_trainee_success() throws Exception {
		// given
		SignUpRequest request = createDefaultSignUpRequest("trainee");

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@DisplayName("통합 테스트 - 필수 필드 누락으로 회원가입 실패")
	void sign_up_fail_missing_required_field() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest(
			"",
			"trainer",
			"KAKAO",
			"12345",
			"test@kakao.com",
			"홍길동",
			LocalDate.of(1990, 1, 1),
			175.0,
			70.0,
			"테스트 주의사항",
			List.of("체중 감량", "근력 향상")
		);

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("통합 테스트 - 잘못된 회원 타입으로 회원가입 실패")
	void sign_up_fail_invalid_member_type() throws Exception {
		// given
		SignUpRequest request = createDefaultSignUpRequest("invalid_type");

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().is5xxServerError());
	}

	@Test
	@DisplayName("통합 테스트 - PT 목적 누락으로 회원가입 실패")
	void sign_up_empty_goal_contents_fail() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("fcm-token-test", "trainer", "KAKAO", "12345", "test@kakao.com",
			"홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", Collections.emptyList());

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().isBadRequest());
	}
}
