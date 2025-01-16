package com.tnt.global.error.exception;

import com.tnt.global.error.model.ErrorMessage;

public class TnTException extends RuntimeException {

	public TnTException(ErrorMessage errorMessage) {
		super(errorMessage.getMessage());
	}

	public TnTException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage.getMessage(), cause);
	}
}
