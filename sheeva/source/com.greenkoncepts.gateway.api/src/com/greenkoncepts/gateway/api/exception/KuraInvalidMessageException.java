package com.greenkoncepts.gateway.api.exception;

public class KuraInvalidMessageException extends KuraRuntimeException 
{
	private static final long serialVersionUID = -3636897647706575102L;

	public KuraInvalidMessageException(Object argument) {
		super(KuraErrorCode.INVALID_MESSAGE_EXCEPTION, argument);
	}

	public KuraInvalidMessageException(Throwable cause) {
		super(KuraErrorCode.INVALID_MESSAGE_EXCEPTION, cause);
	}

	public KuraInvalidMessageException(Throwable cause, Object argument) {
		super(KuraErrorCode.INVALID_MESSAGE_EXCEPTION, cause, argument);
	}
}
