/* *********************************************************************** *
 * project: org.matsim.*
 * WithinDayControlerListener.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.withinday.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.mobsim.framework.listeners.FixedOrderSimulationListener;
import org.matsim.core.router.TripRouterFactoryImpl;
import org.matsim.core.router.TripRouterFactoryInternal;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.pt.router.TransitRouterFactory;
import org.matsim.withinday.mobsim.WithinDayEngine;
import org.matsim.withinday.mobsim.WithinDayQSimFactory;
import org.matsim.withinday.replanning.identifiers.tools.ActivityReplanningMap;
import org.matsim.withinday.replanning.identifiers.tools.LinkReplanningMap;
import org.matsim.withinday.trafficmonitoring.TravelTimeCollector;
import org.matsim.withinday.trafficmonitoring.TravelTimeCollectorFactory;

/**
 * Attempt to realize functionality provided by WithinDayController by
 * a ControlerListener (similar to what is done by the MultiModalControlerListener).
 * 
 * Note: this class has to be registered as Controller Listener!
 * 
 * @author cdobler
 */
public class WithinDayControlerListener implements StartupListener {

	private static final Logger log = Logger.getLogger(WithinDayControlerListener.class);
	
	private boolean locked = false;
	
	private int numReplanningThreads = 0;
	private TravelTimeCollectorFactory travelTimeCollectorFactory = new TravelTimeCollectorFactory();
	private TravelTimeCollector travelTimeCollector;
	private Set<String> travelTimeCollectorModes = null;
	private ActivityReplanningMap activityReplanningMap;
	private LinkReplanningMap linkReplanningMap;

	private EventsManager eventsManager;
	private Scenario scenario;
	private TravelDisutilityFactory travelDisutilityFactory;
	private LeastCostPathCalculatorFactory leastCostPathCalculatorFactory;
	private TransitRouterFactory transitRouterFactory;

	private WithinDayEngine withinDayEngine;
	private TripRouterFactoryInternal withinDayTripRouterFactory;
	private final FixedOrderSimulationListener fosl = new FixedOrderSimulationListener();
	private final Map<String, TravelTime> multiModalTravelTimes = new HashMap<String, TravelTime>();

	/*
	 * ===================================================================
	 * getters and setters
	 * ===================================================================
	 */	
	public int getNumberOfReplanningThreads() {
		return this.numReplanningThreads;
	}
	
	public void setNumberOfReplanningThreads(int threads) {
		if (locked) throw new RuntimeException(this.getClass().toString() + " configuration has already been locked!");
		this.numReplanningThreads = threads;
	}
	
	public TravelDisutilityFactory getTravelDisutilityFactory() {
		return this.travelDisutilityFactory;
	}

	public void setTravelDisutilityFactory(TravelDisutilityFactory travelDisutilityFactory) {
		if (locked) throw new RuntimeException(this.getClass().toString() + " configuration has already been locked!");
		this.travelDisutilityFactory = travelDisutilityFactory;
	}

	public LeastCostPathCalculatorFactory getLeastCostPathCalculatorFactory() {
		return leastCostPathCalculatorFactory;
	}

	public void setLeastCostPathCalculatorFactory(LeastCostPathCalculatorFactory leastCostPathCalculatorFactory) {
		if (locked) throw new RuntimeException(this.getClass().toString() + " configuration has already been locked!");
		this.leastCostPathCalculatorFactory = leastCostPathCalculatorFactory;
	}

	public TransitRouterFactory getTransitRouterFactory() {
		return transitRouterFactory;
	}

	public void setTransitRouterFactory(TransitRouterFactory transitRouterFactory) {
		if (locked) throw new RuntimeException(this.getClass().toString() + " configuration has already been locked!");
		this.transitRouterFactory = transitRouterFactory;
	}
	
	public void setModesAnalyzedByTravelTimeCollector(Set<String> modes) {
		if (locked) throw new RuntimeException(this.getClass().toString() + " configuration has already been locked!");
		this.travelTimeCollectorModes = modes;
	}
	
	public Set<String> getModesAnalyzedByTravelTimeCollector() {
		return Collections.unmodifiableSet(this.travelTimeCollectorModes);
	}

	public TravelTimeCollector getTravelTimeCollector() {
		return this.travelTimeCollector;
	}

	public ActivityReplanningMap getActivityReplanningMap() {
		return this.activityReplanningMap;
	}

	public LinkReplanningMap getLinkReplanningMap() {
		return this.linkReplanningMap;
	}
	
	public WithinDayEngine getWithinDayEngine() {
		return this.withinDayEngine;
	}
	
	public void setWithinDayTripRouterFactory(TripRouterFactoryInternal tripRouterFactory) {
		if (locked) throw new RuntimeException(this.getClass().toString() + " configuration has already been locked!");
		this.withinDayTripRouterFactory = tripRouterFactory;
	}

	public TripRouterFactoryInternal getWithinDayTripRouterFactory() {
		return this.withinDayTripRouterFactory;
	}
	
	public FixedOrderSimulationListener getFixedOrderSimulationListener() {
		return this.fosl;
	}
	/*
	 * ===================================================================
	 */

	/*
	 * When the Controller Startup Event is created, the EventsManager
	 * has already been initialized. Therefore we can initialize now
	 * all Objects, that have to be registered at the EventsManager.
	 */
	@Override
	public void notifyStartup(StartupEvent event) {
		
		Controler controler = event.getControler();
		controler.getMobsimListeners().add(fosl);
		
		this.scenario = controler.getScenario();
		this.eventsManager = controler.getEvents();
				
		// initialize a withinDayEngine and set WithinDayQSimFactory as MobsimFactory
		this.withinDayEngine = new WithinDayEngine(this.eventsManager);
		WithinDayQSimFactory mobsimFactory = new WithinDayQSimFactory(withinDayEngine);
		controler.setMobsimFactory(mobsimFactory);
		
		if (this.numReplanningThreads == 0) {
			this.numReplanningThreads = controler.getConfig().global().getNumberOfThreads();
		} else log.info("Number of replanning threads has already been set - it is NOT overwritten!");
		
		this.initWithinDayEngine(this.numReplanningThreads);
		this.createAndInitTravelTimeCollector();
		this.createAndInitActivityReplanningMap();
		this.createAndInitLinkReplanningMap();
		
		if (this.leastCostPathCalculatorFactory == null) {
			this.leastCostPathCalculatorFactory = controler.getLeastCostPathCalculatorFactory();
		} else log.info("LeastCostPathCalculatorFactory has already been set - it is NOT overwritten!");
		
		if (this.travelDisutilityFactory == null) {
			this.travelDisutilityFactory = controler.getTravelDisutilityFactory();
		} else log.info("TravelDisutilityFactory has already been set - it is NOT overwritten!");
		
		if (this.transitRouterFactory == null) {
			this.transitRouterFactory = controler.getTransitRouterFactory();
		} else log.info("TransitRouterFactory has already been set - it is NOT overwritten!");
		
		if (this.withinDayTripRouterFactory == null) {
			this.initWithinDayTripRouterFactory();
		} else log.info("WithinDayTripRouterFactory has already been set - it is NOT re-initialized!");
		
		// disable possibility to set factories
		this.locked = true;
	}

	/*
	 * ===================================================================
	 * creation and initialization methods
	 * ===================================================================
	 */
	
	/*
	 * TODO: Add a Within-Day Group to the Config. Then this method
	 * can be called on startup.
	 */
	private void initWithinDayEngine(int numOfThreads) {
		log.info("Initialize WithinDayEngine");
		withinDayEngine.initializeReplanningModules(numOfThreads);
	}

	/*
	 * Use travel times from the travel time collector for car trips.
	 */
	private void initWithinDayTripRouterFactory() {
		this.withinDayTripRouterFactory = new TripRouterFactoryImpl(
				this.scenario,
				this.travelDisutilityFactory,
				this.travelTimeCollector,
				this.leastCostPathCalculatorFactory,
				this.transitRouterFactory);
	}
	
	
	private void createAndInitTravelTimeCollector() {
		this.createAndInitTravelTimeCollector(this.travelTimeCollectorModes);
	}

	private void createAndInitTravelTimeCollector(Set<String> analyzedModes) {
		travelTimeCollector = travelTimeCollectorFactory.createTravelTimeCollector(this.scenario, analyzedModes);
		fosl.addSimulationListener(travelTimeCollector);
		this.eventsManager.addHandler(travelTimeCollector);
	}
	
	private void createAndInitActivityReplanningMap() {
		activityReplanningMap = new ActivityReplanningMap();
		this.eventsManager.addHandler(activityReplanningMap);
		fosl.addSimulationListener(activityReplanningMap);
	}
		
	private void createAndInitLinkReplanningMap() {
		/*
		 * If at least one TravelTime object has been registered, the LinkReplanningMap
		 * has to be configured to run in multi-modal mode.
		 * TODO: what happens if main mode is NOT car?
		 */
		if (this.multiModalTravelTimes.size() > 0) {
			this.multiModalTravelTimes.put(TransportMode.car, new FreeSpeedTravelTime());
			linkReplanningMap = new LinkReplanningMap(this.scenario.getNetwork(), this.multiModalTravelTimes);
		} else linkReplanningMap = new LinkReplanningMap(this.scenario.getNetwork());
		
		this.eventsManager.addHandler(linkReplanningMap);
		fosl.addSimulationListener(linkReplanningMap);
	}
}