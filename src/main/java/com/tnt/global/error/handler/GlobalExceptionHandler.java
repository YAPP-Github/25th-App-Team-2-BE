package com.tnt.global.error.handler;

import static com.tnt.global.error.model.ErrorMessage.*;
import static org.springframework.http.HttpStatus.*;

import java.security.SecureRandom;
import java.time.DateTimeException;
import java.util.List;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.tnt.global.error.exception.ConflictException;
import com.tnt.global.error.exception.NotFoundException;
import com.tnt.global.error.exception.OAuthException;
import com.tnt.global.error.exception.TnTException;
import com.tnt.global.error.exception.UnauthorizedException;
import com.tnt.global.error.model.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String ERROR_KEY_FORMAT = "%n error key : %s";
	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
	private static final int ERROR_KEY_LENGTH = 5;
	private static final String EXCEPTION_CLASS_TYPE_MESSAGE_FORMANT = "%n class type : %s";
	private final SecureRandom secureRandom = new SecureRandom();

	// 필수 파라미터 예외
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ErrorResponse handleMissingServletRequestParameter(MissingServletRequestParameterException exception) {
		String errorMessage = String.format(MISSING_REQUIRED_PARAMETER_ERROR.getMessage(),
			exception.getParameterName());

		log.error(MISSING_REQUIRED_PARAMETER_ERROR.getMessage(), exception.getParameterName(), exception);

		return new ErrorResponse(errorMessage);
	}

	// 파라미터 타입 예외
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ErrorResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
		String errorMessage = String.format(PARAMETER_FORMAT_NOT_CORRECT.getMessage(), exception.getName());

		log.error(PARAMETER_FORMAT_NOT_CORRECT.getMessage(), exception.getName(), exception);

		return new ErrorResponse(errorMessage);
	}

	// @Validated 있는 클래스에서 @RequestParam, @PathVariable 등에 적용된 제약 조건 예외
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	protected ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
		List<String> errors = exception.getConstraintViolations()
			.stream()
			.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
			.toList();
		String errorMessage = String.join(", ", errors);

		log.error(INPUT_VALUE_IS_INVALID.getMessage(), exception);

		return new ErrorResponse(INPUT_VALUE_IS_INVALID.getMessage() + errorMessage);
	}

	// @Valid, @Validated 있는 곳에서 주로 @RequestBody dto 필드에 적용된 검증 어노테이션 유효성 검사 실패 예외
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		String errorMessage = exception.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

		log.error(errorMessage, exception);

		return new ErrorResponse(errorMessage);
	}

	// json 파싱, 날짜/시간 형식 예외
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(value = {HttpMessageNotReadableException.class, DateTimeException.class})
	protected ErrorResponse handleDateTimeParseException(DateTimeException exception) {
		log.error(INVALID_FORMAT_DATETIME.getMessage(), exception);

		return new ErrorResponse(INVALID_FORMAT_DATETIME.getMessage());
	}

	@ResponseStatus(UNAUTHORIZED)
	@ExceptionHandler(value = {UnauthorizedException.class, OAuthException.class})
	protected ErrorResponse handleUnauthorizedException(TnTException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	@ResponseStatus(CONFLICT)
	@ExceptionHandler(ConflictException.class)
	protected ErrorResponse handleConflictException(TnTException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	protected ErrorResponse handleNotFoundException(TnTException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler(IllegalArgumentException.class)
	protected ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	// 기타 500 예외
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RuntimeException.class)
	protected ErrorResponse handleRuntimeException(TnTException exception) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ERROR_KEY_LENGTH; i++) {
			sb.append(CHARACTERS.charAt(secureRandom.nextInt(CHARACTERS.length())));
		}

		String errorKeyInfo = String.format(ERROR_KEY_FORMAT, sb);
		String exceptionTypeInfo = String.format(EXCEPTION_CLASS_TYPE_MESSAGE_FORMANT, exception.getClass());

		log.error("{}{}{}", exception.getMessage(), errorKeyInfo, exceptionTypeInfo, exception);

		return new ErrorResponse(SERVER_ERROR + errorKeyInfo);
	}
}
