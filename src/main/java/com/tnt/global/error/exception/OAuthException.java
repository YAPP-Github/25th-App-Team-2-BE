package com.tnt.global.error.exception;

import com.tnt.global.error.model.ErrorMessage;

public class OAuthException extends TnTException {

	public OAuthException(ErrorMessage errorMessage) {
		super(errorMessage);
	}
}
