/* *********************************************************************** *
 * project: org.matsim.*
 * InformedAgentsFilterFactory.java
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

package playground.christoph.evacuation.withinday.replanning.identifiers;

import org.matsim.withinday.replanning.identifiers.interfaces.AgentFilter;
import org.matsim.withinday.replanning.identifiers.interfaces.AgentFilterFactory;

import playground.christoph.evacuation.withinday.replanning.identifiers.InformedAgentsFilter.FilterType;

public class InformedAgentsFilterFactory implements AgentFilterFactory {

	private final InformedAgentsTracker informedAgentsTracker;
	private final FilterType filterType;
	
	public InformedAgentsFilterFactory(InformedAgentsTracker informedAgentsTracker, FilterType filterType) {
		this.informedAgentsTracker = informedAgentsTracker;
		this.filterType = filterType;
	}
	
	@Override
	public AgentFilter createAgentFilter() {
		return new InformedAgentsFilter(informedAgentsTracker, filterType);
	}

}