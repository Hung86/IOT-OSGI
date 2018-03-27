package com.greenkoncepts.gateway.api.exception;


/**
 * KuraTimeoutException is raised when the attempted operation failed to respond before the timeout exprises.
 */
public class KuraTimeoutException extends KuraException 
{
	private static final long serialVersionUID = -3042470573773974746L;

	public KuraTimeoutException(String message) {
		super(KuraErrorCode.TIMED_OUT, null, message);
	}
	
	public KuraTimeoutException(String message, Throwable cause) {
		super(KuraErrorCode.TIMED_OUT, cause, message);
	}
}
