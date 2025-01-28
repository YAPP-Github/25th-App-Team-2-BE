package com.tnt.common.error.exception;

import com.tnt.common.error.model.ErrorMessage;

public class NotFoundException extends TnTException {

	public NotFoundException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public NotFoundException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
