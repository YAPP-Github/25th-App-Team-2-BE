package com.tnt.presentation.member;

import static com.tnt.domain.constant.Constant.TRAINEE;
import static com.tnt.domain.constant.Constant.TRAINER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tnt.dto.member.request.SignUpRequest;
import com.tnt.infrastructure.redis.AbstractContainerBaseTest;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private MockMultipartFile createProfileImage() {
		return new MockMultipartFile("profileImage", "test.jpg", IMAGE_JPEG_VALUE, "test image content".getBytes());
	}

	private SignUpRequest createDefaultSignUpRequest(String memberType) {
		return new SignUpRequest("fcm-token-test", memberType, "KAKAO", "12345", "test@kakao.com", true, true, true,
			true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));
	}

	private ResultActions performSignUpRequest(SignUpRequest request, MockMultipartFile profileImage) throws Exception {
		return mockMvc.perform(multipart("/members/sign-up")
			.file(new MockMultipartFile("request", "", APPLICATION_JSON_VALUE,
				objectMapper.writeValueAsString(request).getBytes()))
			.file(profileImage)
			.contentType(MULTIPART_FORM_DATA_VALUE));
	}

	@Test
	@DisplayName("통합 테스트 - 트레이너 회원가입 성공")
	void sign_up_trainer_success() throws Exception {
		// given
		SignUpRequest request = createDefaultSignUpRequest(TRAINER);

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@DisplayName("통합 테스트 - 트레이니 회원가입 성공")
	void sign_up_trainee_success() throws Exception {
		// given
		SignUpRequest request = createDefaultSignUpRequest(TRAINEE);

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@DisplayName("통합 테스트 - 잘못된 회원 타입으로 회원가입 실패")
	void sign_up_invalid_member_type_fail() throws Exception {
		// given
		SignUpRequest request = createDefaultSignUpRequest("invalid_type");

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("통합 테스트 - 필수 필드 누락으로 회원가입 실패")
	void sign_up_missing_required_field_fail() throws Exception {
		// given
		SignUpRequest request = new SignUpRequest("", TRAINER, "KAKAO", "12345", "test@kakao.com", null, null, true,
			true, "홍길동", LocalDate.of(1990, 1, 1), 175.0, 70.0, "테스트 주의사항", List.of("체중 감량", "근력 향상"));

		// when
		ResultActions result = performSignUpRequest(request, createProfileImage());

		// then
		result.andExpect(status().isBadRequest());
	}
}
