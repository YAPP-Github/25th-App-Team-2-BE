package com.tnt.common.error.exception;

import com.tnt.common.error.model.ErrorMessage;

public class ImageException extends TnTException {

	public ImageException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public ImageException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
