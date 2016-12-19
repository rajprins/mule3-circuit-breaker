/**
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
	 * The amount of failures (exceptions thrown/caught) until the circuit breaker is tripped.
	 */
	@Configurable
	@Default("3")
	@Placement(order = 1, group = "Circuit Breaker Settings")
	@FriendlyName("Failure threshold")
	private int tripThreshold;

	/**
	 * How long to wait (in milliseconds) until the breaker's failure count is reset.
	 */
	@Configurable
	@Default("60000")
	@Placement(order = 2, group = "Circuit Breaker Settings")
	@FriendlyName("Failure count reset time (ms)")
	private long tripResetTime;

	/**
	 * The name of this breaker.
	 */
	@Configurable
	@Placement(order = 3, group = "Circuit Breaker Settings")
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
