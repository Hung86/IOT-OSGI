package com.greenkoncepts.gateway.api.exception;


/**
 * KuraTooManyInflightMessagesException is raised if a publish is attempted when there are already too many messages queued for publishing.
 */
public class KuraTooManyInflightMessagesException extends KuraException 
{
	private static final long serialVersionUID = 8759879149959567323L;

	public KuraTooManyInflightMessagesException(Object argument) {
		super(KuraErrorCode.TOO_MANY_INFLIGHT_MESSAGES, null, argument);
	}
	
	public KuraTooManyInflightMessagesException(Throwable cause, Object argument) {
		super(KuraErrorCode.TOO_MANY_INFLIGHT_MESSAGES, cause, argument);
	}
}
