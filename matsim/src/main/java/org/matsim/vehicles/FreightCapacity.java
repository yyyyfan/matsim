/* *********************************************************************** *
 * project: org.matsim.*
 * BasicFreightCapacity
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.vehicles;

public interface FreightCapacity {
	
	public double getVolume();

	public void setVolume(double cubicMeters);

	public double getWeight();

	public void setWeight(double tons);

	public int getUnits();

	public void setUnits(int units);
	
}