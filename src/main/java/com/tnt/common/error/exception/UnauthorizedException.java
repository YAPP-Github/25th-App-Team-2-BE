package com.tnt.common.error.exception;

import com.tnt.common.error.model.ErrorMessage;

public class UnauthorizedException extends TnTException {

	public UnauthorizedException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public UnauthorizedException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
