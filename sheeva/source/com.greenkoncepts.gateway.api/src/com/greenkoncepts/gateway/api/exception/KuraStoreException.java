package com.greenkoncepts.gateway.api.exception;


/**
 * KuraStoreException is raised when a failure occurred during a persistence operation.
 */
public class KuraStoreException extends KuraException 
{
	private static final long serialVersionUID = -3405089623687223551L;
	
	public KuraStoreException(Object argument) {
		super(KuraErrorCode.STORE_ERROR, null, argument);
	}
	
	public KuraStoreException(Throwable cause, Object argument) {
		super(KuraErrorCode.STORE_ERROR, cause, argument);
	}
}
