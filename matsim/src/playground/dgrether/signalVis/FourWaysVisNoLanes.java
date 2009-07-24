/* *********************************************************************** *
 * project: org.matsim.*
 * FourWaysVis
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package playground.dgrether.signalVis;

import org.matsim.vis.otfvis.executables.OTFVisController;



public class FourWaysVisNoLanes {

	public static final String TESTINPUTDIR = "test/input/org/matsim/signalsystems/TravelTimeFourWaysTest/";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String netFile = TESTINPUTDIR + "network.xml.gz";
		String lanesFile  = TESTINPUTDIR + "testLaneDefinitions_v1.1.xml";
		String popFile = TESTINPUTDIR + "plans.xml.gz";
		String signalFile = TESTINPUTDIR + "testSignalSystems_v1.1.xml";
		String signalConfigFile = TESTINPUTDIR + "testSignalSystemConfigurations_v1.1.xml";
		String configFile = TESTINPUTDIR + "config.xml";
		
		String[] netArray = {netFile};
		
		OTFVisController controller = new OTFVisController(configFile);
		controller.setOverwriteFiles(true);
		controller.run();
		
		//this is run
//		OTFVis.playNetwork(netArray);
		
		
		//this is hack
//		ScenarioImpl scenario = new ScenarioImpl();
//		scenario.getConfig().network().setInputFile(netFile);
//		scenario.getConfig().plans().setInputFile(popFile);
//		
//		scenario.getConfig().network().setLaneDefinitionsFile(lanesFile);
//		scenario.getConfig().scenario().setUseLanes(true);
//		
//		scenario.getConfig().signalSystems().setSignalSystemFile(signalFile);
//		scenario.getConfig().signalSystems().setSignalSystemConfigFile(signalConfigFile);
//		scenario.getConfig().scenario().setUseSignalSystems(true);
//		
//		ScenarioLoader loader = new ScenarioLoader(scenario);
//		loader.loadScenario();
//		
//		Events events = new Events();
//		
//		
//		DgOnTheFlyQueueSimQuad client = new DgOnTheFlyQueueSimQuad(scenario, events);
//		client.setLaneDefinitions(scenario.getLaneDefinitions());
//		client.setSignalSystems(scenario.getSignalSystems(), scenario.getSignalSystemConfigurations());
//		client.run();
		
		
	}

}
