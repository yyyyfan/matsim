/* *********************************************************************** *
 * project: org.matsim.*
 * DgSylviaSignalModelFactory
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
package playground.dgrether.signalsystems.sylvia.model;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.signals.builder.DefaultSignalModelFactory;
import org.matsim.contrib.signals.builder.SignalModelFactory;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalPlanData;
import org.matsim.contrib.signals.model.DatabasedSignalPlan;
import org.matsim.contrib.signals.model.SignalController;
import org.matsim.contrib.signals.model.SignalPlan;
import org.matsim.contrib.signals.model.SignalSystem;

import com.google.inject.Inject;

import playground.dgrether.signalsystems.LinkSensorManager;
import playground.dgrether.signalsystems.sylvia.controler.DgSylviaConfig;
import playground.dgrether.signalsystems.sylvia.data.DgSylviaPreprocessData;
import playground.dgrether.signalsystems.sylvia.model.SylviaSignalController.Builder;


/**
 * @author dgrether
 * @author tthunig
 *
 */
public final class SylviaSignalModelFactory implements SignalModelFactory {

	
	private static final Logger log = Logger.getLogger(SylviaSignalModelFactory.class);
	
	private SignalModelFactory delegate = new DefaultSignalModelFactory();
	
	private Builder builder;

	@Inject 
	public SylviaSignalModelFactory(LinkSensorManager sensorManager, DgSylviaConfig sylviaConfig, Scenario scenario) {
		SignalsData signalsData = (SignalsData) scenario.getScenarioElement(SignalsData.ELEMENT_NAME);
		this.builder = new SylviaSignalController.Builder(sylviaConfig, sensorManager, signalsData);
	}

	@Override
	public SignalSystem createSignalSystem(Id<SignalSystem> id) {
		return this.delegate.createSignalSystem(id);
	}

	@Override
	public SignalController createSignalSystemController(String controllerIdentifier, SignalSystem signalSystem) {
		if (SylviaSignalController.IDENTIFIER.equals(controllerIdentifier)){
			log.info("Creating " + SylviaSignalController.IDENTIFIER);
			return builder.build(signalSystem);
		}
		return this.delegate.createSignalSystemController(controllerIdentifier, signalSystem);
	}

	@Override
	public SignalPlan createSignalPlan(SignalPlanData planData) {
		DatabasedSignalPlan plan = (DatabasedSignalPlan) this.delegate.createSignalPlan(planData);
		if (planData.getId().toString().startsWith(DgSylviaPreprocessData.SYLVIA_PREFIX)){
			return new DgSylviaSignalPlan(plan);
		}
		return plan;
	}

}