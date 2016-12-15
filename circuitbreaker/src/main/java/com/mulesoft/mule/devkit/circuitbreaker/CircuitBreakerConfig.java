package com.mulesoft.mule.devkit.circuitbreaker;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.param.Default;

/**
 * Configuration holder for Circuit Breaker
 * 
 * @author Roy Prins
 *
 */
@Configuration(configElementName = "config", friendlyName = "config")
public class CircuitBreakerConfig {
	/**
	 * The amount of failures (exceptions) until the circuit breaker is tripped.
	 */
	@Configurable
	@Default("3")
	@Placement(group = "Circuit Breaker Settings")
	@FriendlyName("Failure threshold")
	private int tripThreshold;

	/**
	 * How long to wait (in milliseconds) until the breaker is automatically reset.
	 */
	@Configurable
	@Default("60000")
	@Placement(group = "Circuit Breaker Settings")
	@FriendlyName("Failure count reset time (ms)")
	private long tripResetTime;

	/**
	 * The name of this breaker.
	 */
	@Configurable
	@Placement(group = "Circuit Breaker Settings")
	private String breakerName;

	public void setTripThreshold(int tripThreshold) {
		this.tripThreshold = tripThreshold;
	}

	public int getTripThreshold() {
		return tripThreshold;
	}

	public void setTripResetTime(long tripTimeout) {
		this.tripResetTime = tripTimeout;
	}

	public long getTripResetTime() {
		return tripResetTime;
	}

	public void setBreakerName(String breakerName) {
		this.breakerName = breakerName;
	}

	public String getBreakerName() {
		return breakerName;
	}

}
