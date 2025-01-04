package com.tnt.global.error.handler;

import java.security.SecureRandom;
import java.time.DateTimeException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.tnt.global.error.model.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String DEFAULT_ERROR_MESSAGE = "관리자에게 문의해 주세요.";
	private static final String ERROR_KEY_FORMAT = "%n error key : %s";
	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
	private static final int ERROR_KEY_LENGTH = 5;
	private static final String EXCEPTION_CLASS_TYPE_MESSAGE_FORMANT = "%n class type : %s";
	private final SecureRandom secureRandom = new SecureRandom();

	// 필수 파라미터 예외
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ErrorResponse handleMissingServletRequestParameter(
		MissingServletRequestParameterException exception) {
		log.warn("Required request parameter is missing: {}", exception.getParameterName());
		String errorMessage = String.format("필수 파라미터 '%s'가 누락되었습니다.", exception.getParameterName());
		return new ErrorResponse(errorMessage);
	}

	// 파라미터 타입 예외
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ErrorResponse handleMethodArgumentTypeMismatch(
		MethodArgumentTypeMismatchException exception) {
		log.warn("Type mismatch for parameter: {}. Required type: {}", exception.getName(),
			exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "unknown");
		String errorMessage;
		if (exception.getRequiredType() != null) {
			errorMessage = String.format("파라미터 '%s'의 형식이 올바르지 않습니다. 예상 타입: %s",
				exception.getName(), exception.getRequiredType().getSimpleName());
		} else {
			errorMessage = String.format("파라미터 '%s'의 형식이 올바르지 않습니다.", exception.getName());
		}
		return new ErrorResponse(errorMessage);
	}

	// @Validated 있는 클래스에서 @RequestParam, @PathVariable 등에 적용된 제약 조건 예외
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
		log.warn("Constraint violation: {}", exception.getMessage());

		List<String> errors = exception.getConstraintViolations()
			.stream()
			.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
			.toList();

		String errorMessage = String.join(", ", errors);
		return new ErrorResponse("입력값이 유효하지 않습니다: " + errorMessage);
	}

	// @Valid, @Validated 있는 곳에서 주로 @RequestBody dto 필드에 적용된 검증 어노테이션 유효성 검사 실패 예외
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ErrorResponse handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception) {
		log.warn(exception.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
		return new ErrorResponse(exception.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
	}

	// json 파싱, 날짜/시간 형식 예외
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = {
		HttpMessageNotReadableException.class,
		DateTimeException.class
	})
	public ErrorResponse handleDateTimeParseException(DateTimeException exception) {
		log.warn(exception.getMessage());
		return new ErrorResponse("DateTime 형식이 잘못되었습니다. 서버 관리자에게 문의해 주세요.");
	}

	// 존재x 예외
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler
	public ErrorResponse handleNotFoundException(RuntimeException exception) {
		log.warn(exception.getMessage());
		return new ErrorResponse(exception.getMessage());
	}

	// 존재 예외
	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler
	public ErrorResponse handleExistException(RuntimeException exception) {
		log.warn(exception.getMessage());
		return new ErrorResponse(exception.getMessage());
	}

	// 커스텀 예외
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = {
		MaxUploadSizeExceededException.class
	})
	public ErrorResponse handleCustomBadRequestException(final RuntimeException exception) {
		log.warn(exception.getMessage());
		return new ErrorResponse(exception.getMessage());
	}

	// 기타 500 예외
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RuntimeException.class)
	public ErrorResponse handleRuntimeException(final RuntimeException exception) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ERROR_KEY_LENGTH; i++) {
			sb.append(CHARACTERS.charAt(secureRandom.nextInt(CHARACTERS.length())));
		}

		String errorKeyInfo = String.format(ERROR_KEY_FORMAT, sb);
		String exceptionTypeInfo = String.format(EXCEPTION_CLASS_TYPE_MESSAGE_FORMANT, exception.getClass());
		log.error("{}{}{}", exception.getMessage(), errorKeyInfo, exceptionTypeInfo);
		return new ErrorResponse(DEFAULT_ERROR_MESSAGE + errorKeyInfo);
	}
}
