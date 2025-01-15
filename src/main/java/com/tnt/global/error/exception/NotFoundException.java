package com.tnt.global.error.exception;

import com.tnt.global.error.model.ErrorMessage;

public class NotFoundException extends TnTException {

	public NotFoundException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public NotFoundException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
