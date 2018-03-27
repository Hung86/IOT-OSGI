package com.greenkoncepts.gateway.api.exception;


/**
 * KuraStoreCapacityReachedException is raised when a message can not be appended
 * to the publishing queue as the internal database buffer has reached its 
 * capacity for messages that are not yet published or they are still in transit.
 */
public class KuraStoreCapacityReachedException extends KuraStoreException 
{
	private static final long serialVersionUID = 2622483579047285733L;

	public KuraStoreCapacityReachedException(Object argument) {
		super(argument);
	}
}
