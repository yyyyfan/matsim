/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.andreas.P2.stats.abtractPAnalysisModules;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.TransitDriverStartsEvent;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.events.handler.TransitDriverStartsEventHandler;


/**
 * Count the number of vehicle-meter per ptModes specified.
 * 
 * @author aneumann
 *
 */
public class CountVehicleMeterPerMode extends AbstractPAnalyisModule implements TransitDriverStartsEventHandler, LinkEnterEventHandler{
	
	private final static Logger log = Logger.getLogger(CountVehicleMeterPerMode.class);
	
	private Network network;
	private HashMap<Id, String> vehId2ptModeMap;
	private HashMap<String, Double> ptMode2CountMap;
	
	public CountVehicleMeterPerMode(Network network){
		super(CountVehicleMeterPerMode.class.getSimpleName());
		this.network = network;
		log.info("enabled");
	}

	@Override
	public String getResult() {
		StringBuffer strB = new StringBuffer();
		for (String ptMode : this.ptModes) {
			strB.append(", " + this.ptMode2CountMap.get(ptMode));
		}
		return strB.toString();
	}
	
	@Override
	public void reset(int iteration) {
		super.reset(iteration);
		this.vehId2ptModeMap = new HashMap<Id, String>();
		this.ptMode2CountMap = new HashMap<String, Double>();
	}

	@Override
	public void handleEvent(TransitDriverStartsEvent event) {
		super.handleEvent(event);
		String ptMode = this.lineIds2ptModeMap.get(event.getTransitLineId());
		if (ptMode == null) {
			log.warn("Should not happen");
			ptMode = "no valid pt mode found";
		}
		this.vehId2ptModeMap.put(event.getVehicleId(), ptMode);
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		String ptMode = this.vehId2ptModeMap.get(event.getVehicleId());
		if (ptMode == null) {
			ptMode = "nonPtMode";
		}
		if (ptMode2CountMap.get(ptMode) == null) {
			ptMode2CountMap.put(ptMode, new Double(0.0));
		}

		ptMode2CountMap.put(ptMode, new Double(ptMode2CountMap.get(ptMode) + this.network.getLinks().get(event.getLinkId()).getLength()));
	}
}