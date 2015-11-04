/* *********************************************************************** *
 * project: org.matsim.*
 * PTTravelTime.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.core.mobsim.qsim.multimodalsimengine.router.util;

import java.util.Set;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

/**
 * 
 * @author cdobler
 */
public class PTTravelTime implements TravelTime {

	private final TravelTime carTravelTime;	// PT speed does not depend on a passenger, therefore not personalizable
	private final TravelTime walkTravelTime;
	private final double ptScaleFactor;
	
	public PTTravelTime(PlansCalcRouteConfigGroup plansCalcRouteConfigGroup, 
			TravelTime carTravelTime, TravelTime walkTravelTime) {
		this.ptScaleFactor = plansCalcRouteConfigGroup.getPtSpeedFactor();
		this.carTravelTime = carTravelTime;
		this.walkTravelTime = walkTravelTime;
	}

	@Override
	public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
		/*
		 * If it is a pt or car link, we use car travel times. Else we check whether it is
		 * a bike / walk link - if it is one, we use walk travel times.
		 */
		Set<String> allowedModes = link.getAllowedModes();
		if (allowedModes.contains(TransportMode.car) || allowedModes.contains(TransportMode.pt)) {
			return ptScaleFactor * carTravelTime.getLinkTravelTime(link, time, person, vehicle);
		}
		else if (allowedModes.contains(TransportMode.bike) ||allowedModes.contains(TransportMode.walk)) {
			return walkTravelTime.getLinkTravelTime(link, time, person, vehicle);
		}
		
		return link.getLength() / 1.0;
	}
	
}