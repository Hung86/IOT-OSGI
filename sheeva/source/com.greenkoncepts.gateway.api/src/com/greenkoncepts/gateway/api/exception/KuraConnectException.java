
package com.greenkoncepts.gateway.api.exception;


/**
 * KuraConnectException is raised during connect failures. 
 */
public class KuraConnectException extends KuraException 
{
	private static final long serialVersionUID = 5894832757268538532L;

	public KuraConnectException(Object argument) {
		super(KuraErrorCode.CONNECTION_FAILED, null, argument);
	}
	
	public KuraConnectException(Throwable cause, Object argument) {
		super(KuraErrorCode.CONNECTION_FAILED, cause, argument);
	}
}
