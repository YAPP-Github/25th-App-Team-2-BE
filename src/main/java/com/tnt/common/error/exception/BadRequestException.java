package com.tnt.common.error.exception;

import com.tnt.common.error.model.ErrorMessage;

public class BadRequestException extends TnTException {

	public BadRequestException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public BadRequestException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
