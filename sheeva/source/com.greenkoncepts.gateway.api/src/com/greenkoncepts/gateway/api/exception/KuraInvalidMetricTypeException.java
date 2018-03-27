package com.greenkoncepts.gateway.api.exception;

public class KuraInvalidMetricTypeException extends KuraRuntimeException 
{
	private static final long serialVersionUID = 3811194468467381264L;

	public KuraInvalidMetricTypeException(Object argument) {
		super(KuraErrorCode.INVALID_METRIC_EXCEPTION, argument);
	}

	public KuraInvalidMetricTypeException(Throwable cause, Object argument) {
		super(KuraErrorCode.INVALID_METRIC_EXCEPTION, cause, argument);
	}
}
