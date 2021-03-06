/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package org.matsim.contrib.ev;

import org.matsim.contrib.ev.charging.ChargingModule;
import org.matsim.contrib.ev.data.ElectricFleetModule;
import org.matsim.contrib.ev.discharging.DischargingModule;
import org.matsim.contrib.ev.stats.EvStatsModule;
import org.matsim.core.controler.AbstractModule;

public class EvModule extends AbstractModule {

	@Override
	public void install() {
		EvConfigGroup evCfg = EvConfigGroup.get(getConfig());
		install(new ElectricFleetModule(evCfg));
		install(new ChargingModule(evCfg));
		install(new DischargingModule(evCfg));
		install(new EvStatsModule(evCfg));
	}
}
