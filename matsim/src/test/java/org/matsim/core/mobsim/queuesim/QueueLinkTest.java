/* *********************************************************************** *
 * project: org.matsim.*
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

package org.matsim.core.mobsim.queuesim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.qsim.agents.DefaultAgentFactory;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;

/**
 * @author dgrether
 * @author mrieser
 */
public class QueueLinkTest extends MatsimTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		loadConfig(null);
	}

	public void testInit() {
		Fixture f = new Fixture();
		assertNotNull(f.qlink1);
		assertEquals(1.0, f.qlink1.getSimulatedFlowCapacity(), EPSILON);
		assertEquals(1.0, f.qlink1.getStorageCapacity(), EPSILON);
		assertEquals(f.link1, f.qlink1.getLink());
	}


	public void testAdd() {
		Fixture f = new Fixture();
		QueueVehicle v = new QueueVehicle(f.basicVehicle);
		PersonImpl p = new PersonImpl(new IdImpl("1"));
		p.addPlan(new PlanImpl());
		v.setDriver(createQueuePersonAgent(p, f.qSim));
		f.qlink1.addFromIntersection(v, 0.0);
		assertEquals(1, f.qlink1.vehOnLinkCount());
		assertFalse(f.qlink1.hasSpace());
		assertTrue(f.qlink1.bufferIsEmpty());
	}

	/**
	 * Tests that vehicles driving on a link are found with {@link QueueLink#getVehicle(Id)}
	 * and {@link QueueLink#getAllVehicles()}.
	 *
	 * @author mrieser
	 */
	public void testGetVehicle_Driving() {
		Fixture f = new Fixture();
		Id id1 = new IdImpl("1");
		QueueVehicle veh = new QueueVehicle(f.basicVehicle);
		PersonImpl p = new PersonImpl(new IdImpl(23));
		Plan plan = new PlanImpl();
		p.addPlan(plan);
		plan.addActivity(new ActivityImpl("home", f.link1.getId()));
		Leg leg = new LegImpl(TransportMode.car);
		leg.setRoute(new LinkNetworkRouteImpl(f.link1.getId(), f.link2.getId()));
		plan.addLeg(leg);
		plan.addActivity(new ActivityImpl("work", f.link2.getId()));
		MobsimDriverAgent driver = createQueuePersonAgent(p, f.qSim);
		veh.setDriver(driver);
		driver.setVehicle(veh);
		driver.endActivityAndComputeNextState(0);
		
		// start test, check initial conditions
		assertTrue(f.qlink1.bufferIsEmpty());
		assertEquals(0, f.qlink1.vehOnLinkCount());
		assertNull(f.qlink1.getVehicle(id1));
		assertEquals(0, f.qlink1.getAllVehicles().size());

		// add a vehicle, it should be now in the vehicle queue
		f.qlink1.addFromIntersection(veh, 0.0);
		assertTrue(f.qlink1.bufferIsEmpty());
		assertEquals(1, f.qlink1.vehOnLinkCount());
		assertEquals("vehicle not found on link.", veh, f.qlink1.getVehicle(id1));
		assertEquals(1, f.qlink1.getAllVehicles().size());

		// time step 1, vehicle should be now in the buffer
		f.qlink1.moveLink(1.0);
		assertFalse(f.qlink1.bufferIsEmpty());
		assertEquals(0, f.qlink1.vehOnLinkCount());
		assertEquals("vehicle not found in buffer.", veh, f.qlink1.getVehicle(id1));
		assertEquals(1, f.qlink1.getAllVehicles().size());
		assertEquals(veh, f.qlink1.getAllVehicles().iterator().next());

		// time step 2, vehicle leaves link
		f.qlink1.moveLink(2.0);
		assertEquals(veh, f.qlink1.popFirstFromBuffer());
		assertTrue(f.qlink1.bufferIsEmpty());
		assertEquals(0, f.qlink1.vehOnLinkCount());
		assertNull("vehicle should not be on link anymore.", f.qlink1.getVehicle(id1));
		assertEquals(0, f.qlink1.getAllVehicles().size());
	}


	/**
	 * Tests that vehicles departing on a link are found with {@link QueueLink#getVehicle(Id)}
	 * and {@link QueueLink#getAllVehicles()}.
	 *
	 * @author mrieser
	 */
	public void testGetVehicle_Departing() {
		Fixture f = new Fixture();
		Id id1 = new IdImpl("1");

		QueueSimulation qsim = new QueueSimulation(f.scenario, EventsUtils.createEventsManager());

		QueueVehicle veh = new QueueVehicle(f.basicVehicle);
		PersonImpl p = new PersonImpl(new IdImpl(80));
		Plan plan = new PlanImpl();
		p.addPlan(plan);
		plan.addActivity(new ActivityImpl("home", f.link1.getId()));
		Leg leg = new LegImpl(TransportMode.car);
		leg.setRoute(new LinkNetworkRouteImpl(f.link1.getId(), f.link2.getId()));
		plan.addLeg(leg);
		plan.addActivity(new ActivityImpl("work", f.link2.getId()));
		MobsimDriverAgent driver = createQueuePersonAgent(p, qsim);
		veh.setDriver(driver);
		driver.setVehicle(veh);
		driver.endActivityAndComputeNextState(0);

		// start test, check initial conditions
		assertTrue(f.qlink1.bufferIsEmpty());
		assertEquals(0, f.qlink1.vehOnLinkCount());
		assertEquals(0, f.qlink1.getAllVehicles().size());

		f.qlink1.addDepartingVehicle(veh);
		assertTrue(f.qlink1.bufferIsEmpty());
		assertEquals(0, f.qlink1.vehOnLinkCount());
		assertEquals("vehicle not found in waiting list.", veh, f.qlink1.getVehicle(id1));
		assertEquals(1, f.qlink1.getAllVehicles().size());
		assertEquals(veh, f.qlink1.getAllVehicles().iterator().next());

		// time step 1, vehicle should be now in the buffer
		f.qlink1.moveLink(1.0);
		assertFalse(f.qlink1.bufferIsEmpty());
		assertEquals(0, f.qlink1.vehOnLinkCount());
		assertEquals("vehicle not found in buffer.", veh, f.qlink1.getVehicle(id1));
		assertEquals(1, f.qlink1.getAllVehicles().size());

		// vehicle leaves link
		assertEquals(veh, f.qlink1.popFirstFromBuffer());
		assertTrue(f.qlink1.bufferIsEmpty());
		assertEquals(0, f.qlink1.vehOnLinkCount());
		assertNull("vehicle should not be on link anymore.", f.qlink1.getVehicle(id1));
		assertEquals(0, f.qlink1.getAllVehicles().size());
	}

	/**
	 * Tests the behavior of the buffer (e.g. that it does not accept too many vehicles).
	 *
	 * @author mrieser
	 */
	public void testBuffer() {
		ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		NetworkImpl network = (NetworkImpl) scenario.getNetwork();
		network.setCapacityPeriod(1.0);
		Node node1 = network.createAndAddNode(new IdImpl("1"), new CoordImpl(0, 0));
		Node node2 = network.createAndAddNode(new IdImpl("2"), new CoordImpl(1, 0));
		Node node3 = network.createAndAddNode(new IdImpl("3"), new CoordImpl(2, 0));
		Link link1 = network.createAndAddLink(new IdImpl("1"), node1, node2, 1.0, 1.0, 1.0, 1.0);
		Link link2 = network.createAndAddLink(new IdImpl("2"), node2, node3, 1.0, 1.0, 1.0, 1.0);
		QueueSimulation qsim = new QueueSimulation(scenario, EventsUtils.createEventsManager());
		QueueNetwork queueNetwork = new QueueNetwork(network, qsim);
		QueueLink qlink = queueNetwork.getQueueLinks().get(new IdImpl("1"));

		QueueVehicle v1 = new QueueVehicle(new VehicleImpl(new IdImpl("1"), new VehicleTypeImpl(new IdImpl("defaultVehicleType"))));
		PersonImpl p = new PersonImpl(new IdImpl("1"));
		PlanImpl plan = p.createAndAddPlan(true);
		try {
			plan.createAndAddActivity("h", link1.getId());
			LegImpl leg = plan.createAndAddLeg(TransportMode.car);
			NetworkRoute route = new LinkNetworkRouteImpl(link1.getId(), link2.getId());
			leg.setRoute(route);
			route.setLinkIds(link1.getId(), null, link2.getId());
			leg.setRoute(route);
			plan.createAndAddActivity("w", link2.getId());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		MobsimDriverAgent pa1 = createQueuePersonAgent(p, qsim);
		v1.setDriver(pa1);
		pa1.setVehicle(v1);
		pa1.endActivityAndComputeNextState(0.0);

		QueueVehicle v2 = new QueueVehicle(new VehicleImpl(new IdImpl("2"), new VehicleTypeImpl(new IdImpl("defaultVehicleType"))));
		MobsimDriverAgent pa2 = createQueuePersonAgent(p, qsim);
		v2.setDriver(pa2);
		pa2.setVehicle(v2);
		pa2.endActivityAndComputeNextState(0.0);

		// start test
		assertTrue(qlink.bufferIsEmpty());
		assertEquals(0, qlink.vehOnLinkCount());
		// add v1
		qlink.addFromIntersection(v1, 0.0);
		assertEquals(1, qlink.vehOnLinkCount());
		assertTrue(qlink.bufferIsEmpty());
		// time step 1, v1 is moved to buffer
		qlink.moveLink(1.0);
		assertEquals(0, qlink.vehOnLinkCount());
		assertFalse(qlink.bufferIsEmpty());
		// add v2, still time step 1
		qlink.addFromIntersection(v2, 1.0);
		assertEquals(1, qlink.vehOnLinkCount());
		assertFalse(qlink.bufferIsEmpty());
		// time step 2, v1 still in buffer, v2 cannot enter buffer, so still on link
		qlink.moveLink(2.0);
		assertEquals(1, qlink.vehOnLinkCount());
		assertFalse(qlink.bufferIsEmpty());
		// v1 leaves buffer
		assertEquals(v1, qlink.popFirstFromBuffer());
		assertEquals(1, qlink.vehOnLinkCount());
		assertTrue(qlink.bufferIsEmpty());
		// time step 3, v2 moves to buffer
		qlink.moveLink(3.0);
		assertEquals(0, qlink.vehOnLinkCount());
		assertFalse(qlink.bufferIsEmpty());
		// v2 leaves buffer
		assertEquals(v2, qlink.popFirstFromBuffer());
		assertEquals(0, qlink.vehOnLinkCount());
		assertTrue(qlink.bufferIsEmpty());
		// time step 4, empty link
		qlink.moveLink(4.0);
		assertEquals(0, qlink.vehOnLinkCount());
		assertTrue(qlink.bufferIsEmpty());
	}

	public void testStorageSpaceDifferentVehicleSizes() {
		Fixture f = new Fixture();
		PersonImpl p = new PersonImpl(new IdImpl(5));
		PlanImpl plan = p.createAndAddPlan(true);
		try {
			plan.createAndAddActivity("h", f.link1.getId());
			LegImpl leg = plan.createAndAddLeg(TransportMode.car);
			NetworkRoute route = new LinkNetworkRouteImpl(f.link1.getId(), f.link2.getId());
			leg.setRoute(route);
			route.setLinkIds(f.link1.getId(), null, f.link2.getId());
			leg.setRoute(route);
			plan.createAndAddActivity("w", f.link2.getId());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		VehicleType vehType = new VehicleTypeImpl(new IdImpl("defaultVehicleType"));
		QueueVehicle veh1 = new QueueVehicle(new VehicleImpl(new IdImpl(1), vehType));
		MobsimDriverAgent agent1 = createQueuePersonAgent( p , f.qSim);
		veh1.setDriver(agent1);
		agent1.endActivityAndComputeNextState(0.0);
		QueueVehicle veh25 = new QueueVehicle(new VehicleImpl(new IdImpl(2), vehType), 2.5);
		MobsimDriverAgent agent25 = createQueuePersonAgent( p , f.qSim);
		veh25.setDriver(agent25);
		agent25.endActivityAndComputeNextState(0.0);
		QueueVehicle veh5 = new QueueVehicle(new VehicleImpl(new IdImpl(3), vehType), 5);
		MobsimDriverAgent agent5 = createQueuePersonAgent( p , f.qSim);
		veh5.setDriver(agent5);
		agent5.endActivityAndComputeNextState(0.0);
		
		assertEquals("wrong initial storage capacity.", 10.0, f.qlink2.getStorageCapacity(), EPSILON);
		f.qlink2.addFromIntersection(veh5, 0.0);  // used vehicle equivalents: 5
		assertTrue(f.qlink2.hasSpace());
		f.qlink2.addFromIntersection(veh5, 0.0);  // used vehicle equivalents: 10
		assertFalse(f.qlink2.hasSpace());

		assertTrue(f.qlink2.bufferIsEmpty());
		f.qlink2.moveLink(5.0); // first veh moves to buffer, used vehicle equivalents: 5
		assertTrue(f.qlink2.hasSpace());
		assertFalse(f.qlink2.bufferIsEmpty());
		f.qlink2.popFirstFromBuffer();  // first veh leaves buffer
		assertTrue(f.qlink2.hasSpace());

		f.qlink2.addFromIntersection(veh25, 5.0); // used vehicle equivalents: 7.5
		f.qlink2.addFromIntersection(veh1, 5.0);  // used vehicle equivalents: 8.5
		f.qlink2.addFromIntersection(veh1, 5.0);  // used vehicle equivalents: 9.5
		assertTrue(f.qlink2.hasSpace());
		f.qlink2.addFromIntersection(veh1, 5.0);  // used vehicle equivalents: 10.5
		assertFalse(f.qlink2.hasSpace());

		f.qlink2.moveLink(6.0); // first veh moves to buffer, used vehicle equivalents: 5.5
		assertTrue(f.qlink2.hasSpace());
		f.qlink2.addFromIntersection(veh1, 6.0);  // used vehicle equivalents: 6.5
		f.qlink2.addFromIntersection(veh25, 6.0); // used vehicle equivalents: 9.0
		f.qlink2.addFromIntersection(veh1, 6.0);  // used vehicle equivalents: 10.0
		assertFalse(f.qlink2.hasSpace());

	}

	/**
	 * Initializes some commonly used data in the tests.
	 *
	 * @author mrieser
	 */
	private static final class Fixture {
		/*package*/ final ScenarioImpl scenario;
		/*package*/ final Link link1;
		/*package*/ final Link link2;
		/*package*/ final QueueNetwork queueNetwork;
		/*package*/ final QueueLink qlink1;
		/*package*/ final QueueLink qlink2;
		/*package*/ final Vehicle basicVehicle;
		/*package*/ final QueueSimulation qSim ; // careful: this qsim is only there so that sim-global variables are defined.  

		/*package*/ Fixture() {
			this.scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
			NetworkImpl network = (NetworkImpl) this.scenario.getNetwork();
			network.setCapacityPeriod(3600.0);
			Node node1 = network.createAndAddNode(new IdImpl("1"), new CoordImpl(0, 0));
			Node node2 = network.createAndAddNode(new IdImpl("2"), new CoordImpl(1, 0));
			Node node3 = network.createAndAddNode(new IdImpl("3"), new CoordImpl(1001, 0));
			this.link1 = network.createAndAddLink(new IdImpl("1"), node1, node2, 1.0, 1.0, 3600.0, 1.0);
			this.link2 = network.createAndAddLink(new IdImpl("2"), node2, node3, 10 * 7.5, 2.0 * 7.5, 3600.0, 1.0);
			this.qSim = new QueueSimulation(this.scenario, EventsUtils.createEventsManager()) ;
			this.queueNetwork = new QueueNetwork(network, qSim);
			this.qlink1 = this.queueNetwork.getQueueLinks().get(new IdImpl("1"));
			this.qlink2 = this.queueNetwork.getQueueLinks().get(new IdImpl("2"));
			this.basicVehicle = new VehicleImpl(new IdImpl("1"), new VehicleTypeImpl(new IdImpl("defaultVehicleType")));
		}

	}
	
	public static MobsimDriverAgent createQueuePersonAgent(Person p, QueueSimulation simulation) {
		MobsimDriverAgent agent = new DefaultAgentFactory( simulation ).createMobsimAgentFromPerson( p );
		simulation.insertAgentIntoMobsim(agent);
		return agent ;
	}

}