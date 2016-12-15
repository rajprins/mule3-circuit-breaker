package com.mulesoft.mule.devkit.circuitbreaker;

import static org.mule.api.config.MuleProperties.OBJECT_STORE_DEFAULT_PERSISTENT_NAME;

import java.util.Date;
import java.util.concurrent.Semaphore;

import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.ExceptionPayload;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Payload;
import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreManager;

/**
 * A module that implements the circuit breaker pattern
 *
 * @Author Roy Prins
 * 
 */
@Connector(name = "circuitbreaker", schemaVersion = "0.0.2-SNAPSHOT", friendlyName = "Circuit Breaker")
public class CircuitBreakerModule {

	private static final Log LOG = LogFactory.getLog(CircuitBreakerModule.class);

	@Config
	CircuitBreakerConfig config;

	private Date breakerTrippedOn;

	private Semaphore objectStoreMutex = new Semaphore(1);

	@Inject
	private MuleContext muleContext;

	@Inject
	private ObjectStoreManager objectStoreManager;

	public void setMuleContext(MuleContext muleContext) {
		this.muleContext = muleContext;
	}

	public MuleContext getMuleContext() {
		return muleContext;
	}

	public void setObjectStoreManager(ObjectStoreManager objectStoreManager) {
		this.objectStoreManager = objectStoreManager;
	}

	public ObjectStoreManager getObjectStoreManager() {
		return objectStoreManager;
	}

	public CircuitBreakerConfig getConfig() {
		return config;
	}

	public void setConfig(CircuitBreakerConfig config) {
		this.config = config;
	}

	/**
	 * Custom processor
	 * <p/>
	 * {@sample.xml ../../../doc/CircuitBreaker-connector.xml.sample
	 * circuitbreaker:filter}
	 *
	 * @param payload
	 *            The message payload
	 * @return Some string
	 * @throws CircuitOpenException
	 *             This exception is thrown once the circuit is tripped
	 */
	@Processor
	public Object filter(@Payload Object payload) throws CircuitOpenException {
		LOG.debug("circuitbeaker:filter applied");

		if (tooFewFailuresToTrip()) {
			LOG.debug("circuitbeaker:filter - failure count too low");
			return payload;
		}

		if (isOpenButTimeoutExceeded()) {
			LOG.debug("circuitbeaker:filter - trip timeout exceeded, count reset");
			breakerTrippedOn = null;
			resetFailureCount();
			return payload;
		}

		LOG.debug("circuitbreaker:filter ACTIVATED");
		throw new CircuitOpenException();
	}

	private boolean tooFewFailuresToTrip() {
		return getFailureCount() < config.getTripThreshold();
	}

	private boolean isOpenButTimeoutExceeded() {
		return breakerTrippedOn != null && timeoutExceeded();
	}

	private boolean timeoutExceeded() {
		return System.currentTimeMillis() - breakerTrippedOn.getTime() > config.getTripResetTime();
	}

	/**
	 * Custom processor
	 * <p/>
	 * {@sample.xml ../../../doc/CircuitBreaker-connector.xml.sample
	 * circuitbreaker:trip}
	 *
	 * @param exceptionMessage
	 *            The exception.
	 * @param tripOnException
	 *            The exception type we should trip on.
	 * @return Some string
	 */
	@Processor
	public Object trip(String tripOnException, MuleEvent exceptionEvent) {
		MuleMessage exceptionMessage = exceptionEvent.getMessage();
		ExceptionPayload exceptionPayload = exceptionMessage.getExceptionPayload();
		LOG.debug("trip triggered [" + exceptionPayload.getException().getCause().getClass().getCanonicalName() + "] ["
				+ exceptionPayload.getException() + "] comparing to [" + tripOnException + "]");
		if (exceptionMatches(exceptionPayload, tripOnException)) {
			LOG.debug("trip matched to: " + tripOnException);
			incrementFailureCount();
			if (isTripThresholdReached()) {
				LOG.debug("failure count matches trip threshold [" + config.getTripThreshold() + "]");
				breakerTrippedOn = new Date();
			}
		}

		return exceptionMessage;
	}

	private boolean isTripThresholdReached() {
		Integer failures = getFailureCount();
		return failures >= config.getTripThreshold();
	}

	/**
	 * Validate that the exception message configured is a super class of the
	 * exception that has been thrown
	 */
	private boolean exceptionMatches(ExceptionPayload exceptionMessage, String tripOnException) {
		try {
			final Class<?> tripOn = Class.forName(tripOnException);
			LOG.debug("trip class to match: " + tripOn);
			final Class<? extends Throwable> cause = ExceptionUtils.getRootCause(exceptionMessage.getException())
					.getClass();
			LOG.debug("trip cause: " + cause);
			return tripOn.isAssignableFrom(cause); // was:
													// cause.getCanonicalName().equals(tripOnException);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(tripOnException + " is not a valid class", e);
		}
	}

	/**
	 * Return the number of exception of the configured type that have been
	 * thrown.
	 */
	private Integer getFailureCount() {
		final String key = failureCountKey();

		final ObjectStore<Integer> store = getAndLockObjectStore();
		try {
			Integer failureCount = 0;
			try {
				if (store.contains(key)) {
					failureCount = store.retrieve(key);
				}
			} catch (Exception e) {
				LOG.error("Could not retrieve key from object-store: " + key, e);
			}
			return failureCount;
		} finally {
			releaseObjectStore();
		}
	}

	/**
	 * Increment the number of failures count when the configured exception is
	 * hit.
	 */
	private void incrementFailureCount() {
		final String key = failureCountKey();

		final ObjectStore<Integer> store = getAndLockObjectStore();
		try {
			Integer failureCount = 0;
			if (store.contains(key)) {
				failureCount = store.retrieve(key);
				store.remove(key);
			}
			store.store(key, failureCount + 1);
		} catch (Exception e) {
			LOG.error("Could not manipulate key in object-store: " + key, e);
		} finally {
			releaseObjectStore();
		}
	}

	/**
	 * Reset the failure count after the circuitbreaker is reset
	 */
	void resetFailureCount() {
		final String key = failureCountKey();

		final ObjectStore<Integer> objectStore = getAndLockObjectStore();
		try {
			if (objectStore.contains(key)) {
				objectStore.remove(key);
			}
			objectStore.store(key, 0);
		} catch (Exception e) {
			LOG.error("Could not remove/store key in object-store: " + key, e);
		} finally {
			releaseObjectStore();
		}
	}

	private ObjectStore<Integer> getAndLockObjectStore() {
		acquireObjectStoreMutex();

		return objectStoreManager.<ObjectStore<Integer>> getObjectStore(OBJECT_STORE_DEFAULT_PERSISTENT_NAME);
	}

	private void acquireObjectStoreMutex() {
		try {
			objectStoreMutex.acquire();
		} catch (InterruptedException e) {
			LOG.error("Could not acquire mutex", e);
			throw new RuntimeException("Could not acquire mutex", e);
		}
	}

	private void releaseObjectStore() {
		objectStoreMutex.release();
	}

	private String failureCountKey() {
		return String.format("%s.failureCount", config.getBreakerName());
	}
}