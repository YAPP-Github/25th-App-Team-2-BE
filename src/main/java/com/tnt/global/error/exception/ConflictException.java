package com.tnt.global.error.exception;

import com.tnt.global.error.model.ErrorMessage;

public class ConflictException extends TnTException {

	public ConflictException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public ConflictException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
