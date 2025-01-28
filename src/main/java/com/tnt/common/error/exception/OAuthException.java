package com.tnt.common.error.exception;

import com.tnt.common.error.model.ErrorMessage;

public class OAuthException extends TnTException {

	public OAuthException(ErrorMessage errorMessage) {
		super(errorMessage);
	}

	public OAuthException(ErrorMessage errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
