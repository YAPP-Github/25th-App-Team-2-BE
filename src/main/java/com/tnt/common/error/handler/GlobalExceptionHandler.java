package com.tnt.common.error.handler;

import static com.tnt.common.error.model.ErrorMessage.INPUT_VALUE_IS_INVALID;
import static com.tnt.common.error.model.ErrorMessage.MISSING_REQUIRED_PARAMETER_ERROR;
import static com.tnt.common.error.model.ErrorMessage.PARAMETER_FORMAT_NOT_CORRECT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.time.DateTimeException;
import java.util.List;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.tnt.common.error.exception.ConflictException;
import com.tnt.common.error.exception.ImageException;
import com.tnt.common.error.exception.NotFoundException;
import com.tnt.common.error.exception.OAuthException;
import com.tnt.common.error.exception.TnTException;
import com.tnt.common.error.exception.UnauthorizedException;
import com.tnt.common.error.model.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

	// json 파싱 예외
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	// 날짜/시간 형식 예외
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(DateTimeException.class)
	protected ErrorResponse handleDateTimeParseException(DateTimeException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(ImageException.class)
	protected ErrorResponse handleImageException(ImageException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	@ResponseStatus(UNAUTHORIZED)
	@ExceptionHandler(value = {UnauthorizedException.class, OAuthException.class})
	protected ErrorResponse handleUnauthorizedException(TnTException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	@ResponseStatus(CONFLICT)
	@ExceptionHandler(ConflictException.class)
	protected ErrorResponse handleConflictException(ConflictException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}

	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	protected ErrorResponse handleNotFoundException(NotFoundException exception) {
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
	protected ErrorResponse handleRuntimeException(RuntimeException exception) {
		log.error(exception.getMessage(), exception);

		return new ErrorResponse(exception.getMessage());
	}
}
