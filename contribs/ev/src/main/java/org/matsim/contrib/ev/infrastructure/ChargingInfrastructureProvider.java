/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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
 * *********************************************************************** *
 */

package org.matsim.contrib.ev.infrastructure;

import java.net.URL;

import org.matsim.api.core.v01.network.Network;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * @author michalm
 */
public class ChargingInfrastructureProvider implements Provider<ChargingInfrastructure> {
	public static final String CHARGERS = "chargers";

	@Inject
	@Named(CHARGERS)
	private Network network;

	private final URL url;

	public ChargingInfrastructureProvider(URL url) {
		this.url = url;
	}

	@Override
	public ChargingInfrastructure get() {
		ChargingInfrastructureSpecification infrastructureSpecification = new ChargingInfrastructureSpecificationImpl();
		new ChargerReader(infrastructureSpecification).parse(url);

		ChargingInfrastructureImpl chargingInfrastructure = new ChargingInfrastructureImpl();
		infrastructureSpecification.getChargerSpecifications()
				.values()
				.stream()
				.map(s -> new ChargerImpl(s, network.getLinks().get(s.getLinkId())))
				.forEach(chargingInfrastructure::addCharger);
		return chargingInfrastructure;
	}
}
