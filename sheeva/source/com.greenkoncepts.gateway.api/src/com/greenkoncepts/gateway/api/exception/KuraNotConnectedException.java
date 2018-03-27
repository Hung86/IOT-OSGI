package com.greenkoncepts.gateway.api.exception;


/**
 * KuraNotConnectedException is raised when the attempted operation requires 
 * an active connection to the remote server while the current state is
 * disconnected.  
 */
public class KuraNotConnectedException extends KuraException 
{
	private static final long serialVersionUID = 5894832757268538532L;

	public KuraNotConnectedException(Object argument) {
		super(KuraErrorCode.NOT_CONNECTED, null, argument);
	}
	
	public KuraNotConnectedException(Throwable cause, Object argument) {
		super(KuraErrorCode.NOT_CONNECTED, cause, argument);
	}
}
