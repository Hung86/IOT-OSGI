package com.greenkoncepts.gateway.api.exception;

import java.util.List;

/**
 * KuraPartialSuccessException is used capture the response 
 * of bulk operations which allow for the failures of some 
 * of their steps.
 * KuraPartialSuccessException.getCauses() will return the
 * exceptions collected during operations for those steps
 * that failed.
 */
public class KuraPartialSuccessException extends KuraException
{
	private static final long serialVersionUID = -350563041335590477L;

	private List<Throwable> m_causes;
	
	public KuraPartialSuccessException(String message, List<Throwable> causes)
	{
		super(KuraErrorCode.PARTIAL_SUCCESS, (Throwable) null, message);
		m_causes = causes; 
	}
	
	
	/**
	 * Returns the list of failures collected during the execution of the bulk operation.
	 * @return causes
	 */
	public List<Throwable> getCauses()
	{
		return m_causes;
	}
}
