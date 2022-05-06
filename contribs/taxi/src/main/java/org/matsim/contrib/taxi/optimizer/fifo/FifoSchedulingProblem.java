/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package org.matsim.contrib.taxi.optimizer.fifo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.taxi.passenger.TaxiRequest;
import org.matsim.contrib.taxi.optimizer.BestDispatchFinder;
import org.matsim.contrib.taxi.scheduler.TaxiScheduler;

public class FifoSchedulingProblem {
	private final Fleet fleet;
	private final TaxiScheduler scheduler;
	private final BestDispatchFinder dispatchFinder;
	private final Scenario scenario;

	public FifoSchedulingProblem(Fleet fleet, TaxiScheduler scheduler, BestDispatchFinder vrpFinder, Scenario scenario) {
		this.fleet = fleet;
		this.scheduler = scheduler;
		this.dispatchFinder = vrpFinder;
		this.scenario = scenario;
	}

	private DvrpVehicle findPrivateVehicle(Fleet fleet, String vehicleId){
		for (DvrpVehicle veh : fleet.getVehicles().values()) {
			if (veh.getId().toString().equals(vehicleId)) {
				return veh;
			}
		}
		return null;
	}


	public void scheduleUnplannedRequests(Collection<TaxiRequest> unplannedRequests) {
		Iterator<TaxiRequest> reqIter = unplannedRequests.iterator();
		Map<Id<Person>,? extends Person> persons = scenario.getPopulation().getPersons();
		BestDispatchFinder.Dispatch<TaxiRequest> best = null;
		while (reqIter.hasNext()) {
			TaxiRequest req = reqIter.next();

			//BestDispatchFinder.Dispatch<TaxiRequest> best = dispatchFinder.findBestVehicleForRequest(req,
			//		fleet.getVehicles().values().stream());
			// We assume that private taxis ids follow the pattern "taxi_{householdId}"
			String carId = "taxi_" + persons.get(req.getPassengerId()).getAttributes().getAttribute("household_id");
			DvrpVehicle privateVehicle = findPrivateVehicle(fleet, carId);

			if (privateVehicle  != null) {
				Stream<DvrpVehicle> privateVehicleStream = Stream.of(privateVehicle);
				best = dispatchFinder.findBestVehicleForRequest(req, privateVehicleStream);
			}

			// TODO search only through available vehicles
			// TODO what about k-nearstvehicle filtering?

			if (best != null) {// TODO won't work with req filtering; use VehicleData to find out when to exit???
				scheduler.scheduleRequest(best.vehicle, best.destination, best.path);
				reqIter.remove();
			}
			/*
			if (best == null) {// TODO won't work with req filtering; use VehicleData to find out when to exit???
				return;
			}

			scheduler.scheduleRequest(best.vehicle, best.destination, best.path);
			reqIter.remove();
			 */
		}
	}
}
