/* *********************************************************************** *
 * project: org.matsim.*
 * Agent2D.java
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

package playground.gregor.sim2d_v3.simulation.floor;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.signalsystems.model.SignalGroupState;

import playground.gregor.sim2d_v3.simulation.floor.forces.Force;
import playground.gregor.sim2d_v3.simulation.floor.forces.deliberative.LinkSwitcher;
import playground.gregor.sim2d_v3.simulation.floor.forces.deliberative.velocityobstacle.Algorithms;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author laemmel
 * 
 */
public class Agent2D {

	private Coordinate currentPosition;
	private final Force force = new Force();
	private double desiredVelocity;
//	private double vx;
//	private double vy;
	private final MobsimDriverAgent pda;
	private final Scenario sc;
	private double currentDesiredVelocity;
	
	//	private final double tau_a = .3;
	
	private double v;
	private double alpha = 0;

	private double earliestUpdate = -1;

	private boolean mentalSwitched = false;
	private final VelocityCalculator velocityCalculator;
	private final LinkSwitcher mentalLinkSwitcher;

	private double sensingRange = 20;
	
	public final double kindness = MatsimRandom.getRandom().nextDouble();
	private final PhysicalAgentRepresentation par;
	private double[] oldBest = new double[2];

	/**
	 * @param p
	 * @param sim2d
	 */
	public Agent2D(MobsimDriverAgent pda, Scenario sc, VelocityCalculator velocityCalculator, LinkSwitcher mlsw, PhysicalAgentRepresentation par) {

		this.pda = pda;
		this.sc = sc;
		this.velocityCalculator = velocityCalculator;
		this.mentalLinkSwitcher = mlsw;
		this.par = par;

		// TODO think about this
		if (velocityCalculator != null) {
			Link currentLink = sc.getNetwork().getLinks().get(pda.getCurrentLinkId());
			Person person = ((PersonDriverAgentImpl) pda).getPerson(); 
			this.desiredVelocity = velocityCalculator.getVelocity(person, currentLink);			
		} else this.desiredVelocity = 1.34;	// workaround for PhantomAgent2
		this.currentDesiredVelocity = this.desiredVelocity;
	}

	/**
	 * @return
	 */
	public Coordinate getPosition() {
		return this.currentPosition;
	}

	public void setPostion(Coordinate pos) {
		this.currentPosition = pos;
		this.par.translate(pos);
	}

	public Force getForce() {
		return this.force;
	}

	/**
	 * @param newPos
	 */
	@Deprecated //use translate instead!
	public void moveToPostion(Coordinate newPos) {
		// TODO check for choose next link and so on ...
		this.currentPosition.setCoordinate(newPos);
	}

	public void translate(double dx, double dy, double vx2, double vy2) {
		this.currentPosition.x += dx;
		this.currentPosition.y += dy;
		setCurrentVelocity(vx2, vy2);
		this.v = Math.sqrt(vx2*vx2+vy2*vy2);
		if (vx2 != 0 || vy2 != 0) {
			this.alpha = 360*Algorithms.getPolarAngle(this.force.getVx(), this.force.getVy())/(2*Math.PI);
		}
		
		this.par.update(this.v,this.alpha, this.currentPosition);

		this.mentalLinkSwitcher.checkForMentalLinkSwitch(this.pda.getCurrentLinkId(), this.pda.chooseNextLinkId(), this);
	}

	/**
	 * @return
	 */
	public double getDesiredVelocity() {
		return this.currentDesiredVelocity;
	}

	@Deprecated //should be private
	public void setCurrentVelocity(double vx, double vy) {
		this.force.setVx(vx);
		this.force.setVy(vy);

	}

	public double getVx() {
		return this.force.getVx();
	}

	public double getVy() {
		return this.force.getVy();
	}

	public double getWeight() {
		return PhysicalAgentRepresentation.AGENT_WEIGHT;
	}

	public void notifyMoveOverNode(Id newLinkId, double time) {
		this.pda.notifyMoveOverNode(newLinkId);
		Link currentLink = this.sc.getNetwork().getLinks().get(newLinkId);
		Person person = ((PersonDriverAgentImpl) this.pda).getPerson(); 
		this.desiredVelocity = this.velocityCalculator.getVelocity(person, currentLink);
		double sp = currentLink.getFreespeed(time);
		this.currentDesiredVelocity = Math.min(this.desiredVelocity, sp);
		this.mentalSwitched = false;
	}
	
	public void informAboutSignalState(SignalGroupState red, double time) {
		if (red == SignalGroupState.RED) {
			this.currentDesiredVelocity = 0.000001; //FIXME can't use 0 here, since we get NaNs in force modules if v0=0;
		} else {
			double sp = this.sc.getNetwork().getLinks().get(this.pda.getCurrentLinkId()).getFreespeed(time);
			this.currentDesiredVelocity = Math.min(this.desiredVelocity, sp);
		}		
	}

	public void switchMental() {
		this.mentalSwitched = true;
	}

	public boolean isMentalSwitched() {
		return this.mentalSwitched;
	}

	public Id getMentalLink() {
		if (this.mentalSwitched) {
			return this.pda.chooseNextLinkId();
		}
		return this.pda.getCurrentLinkId();
	}

	public void setEarliestUpdate(double time) {
		this.earliestUpdate = time;
	}

	public double getEarliestUpdate() {
		return this.earliestUpdate;
	}

	public void setSensingRange(double sens) {
		if (sens > 50) {
			this.sensingRange = 50;
		} else if (sens < .1) {
			this.sensingRange = .1;
		} else {
			this.sensingRange = sens;
		}
	}
	
	public double getSensingRange() {
		return this.sensingRange;
	}

	@Override
	public String toString() {
		return this.currentPosition.toString();
	}
	
	public double getRealActivityEndTime() {
		return this.pda.getActivityEndTime();
	}

	public MobsimDriverAgent getDelegate() {
		return this.pda;
	}

	public PhysicalAgentRepresentation getPhysicalAgentRepresentation() {
		return this.par;
	}

	public void setOldBest(double[] df) {
		this.oldBest = df;
		
	}
	public double[] getOldBest() {
		return this.oldBest;
	}
}