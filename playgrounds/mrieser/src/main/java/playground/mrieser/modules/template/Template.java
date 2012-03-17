/* *********************************************************************** *
 * project: org.matsim.*
 * Template.java
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

package playground.mrieser.modules.template;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.api.experimental.events.AgentArrivalEvent;
import org.matsim.core.api.experimental.events.handler.AgentArrivalEventHandler;
import org.matsim.core.config.Module;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.PersonalizableTravelDisutility;
import org.matsim.core.router.util.PersonalizableTravelTime;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;

import playground.mrieser.modules.MatsimModule;

public class Template implements MatsimModule {

	public void initModule(final Controler c) {
		System.out.println("hello world.");
	}
	
	public void initModule2(final Controler c) {
		// possible things that could get loaded with a module:
		
		
		
		// *** RoutingAlgorithm *********************
		
		LeastCostPathCalculatorFactory routerAlgoFactory = new LeastCostPathCalculatorFactory() {
			public LeastCostPathCalculator createPathCalculator(Network network, TravelDisutility travelCosts, TravelTime travelTimes) {
				return null;
			}
		};
		c.setLeastCostPathCalculatorFactory(routerAlgoFactory);
		// DISCUSS directly set the algo-factory, or just register to be loaded depending on config?
		// what about conflicts, if multiple modules try to set that?
		
		
		
		
		// *** ReplanningModule *********************
		
		PlanStrategyImpl strategy = new PlanStrategyImpl(new RandomPlanSelector());
		c.getStrategyManager().addStrategy(strategy, 0.5);
		// TODO needs improvement, not directly add to StrategyManager, but offer option to be loaded via config
		// register PlanStrategy, PlanSelector, StrategyModule?
		
		
		
		
		// *** ScoringFunction *********************
		
		ScoringFunctionFactory scoringFactory = new ScoringFunctionFactory() {
			public ScoringFunction createNewScoringFunction(Plan plan) {
				return null;
			}
		};
		c.setScoringFunctionFactory(scoringFactory);
		// DISCUSS directly set the sf-factory, or just register to be loaded depending on config?
		
		
		
		
		// *** ConfigGroup *********************
		
		Module templateModule = new Module("Template");
		c.getConfig().addModule("Template", templateModule); // problem: must be called before Controler is fully initialized, but cannot because than its not yet clear which modules need to be laoded. so, could be fixed in Config.java.
		
		
		
		
		// *** EventHandler *********************
		
		AgentArrivalEventHandler eventHandler = new AgentArrivalEventHandler() {
			public void handleEvent(AgentArrivalEvent event) {
			}
			public void reset(int iteration) {
			}
		};
		c.getEvents().addHandler(eventHandler); // problem: getEvents() may return null if not yet initialized
		
		
		
		
		// *** ControlerListener *********************
		
		StartupListener cListener = new StartupListener() {
			public void notifyStartup(StartupEvent event) {
				System.out.println("startup");
			}
		};
		c.addControlerListener(cListener);
		
		
		
		
		// *** Mobsim *********************
		
		// TODO
		
		
		
		// *** TravelCostCalculator *********************
		
		TravelDisutilityFactory travelCostCalculatorFactory = new TravelDisutilityFactory() {

			@Override
			public PersonalizableTravelDisutility createTravelDisutility(
					PersonalizableTravelTime timeCalculator,
					PlanCalcScoreConfigGroup cnScoringGroup) {
				return new PersonalizableTravelDisutility() {

					public double getLinkTravelDisutility(Link link, double time) {
						return 0;
					}
					
					@Override
					public void setPerson(Person person) {
						
					}
					
				};
			}
			
		};
		c.setTravelDisutilityFactory(travelCostCalculatorFactory);
		
		// DISCUSS directly set the calculator, or just register to be loaded depending on config?
		
		
		
		
		// *** TravelTimeCalculator *********************
		
//		TravelTime travelTimeCalculator = new TravelTime() {
//			public double getLinkTravelTime(Link link, double time) {
//				return 0;
//			}
//		};
		// TODO
//		c.setTravelTimeCalculator(travelTimeCalculator);
		
	}

}
