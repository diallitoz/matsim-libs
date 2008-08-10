/* *********************************************************************** *
 * project: org.matsim.*
 * LinkEvent.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.events;

import org.matsim.network.Link;
import org.matsim.network.NetworkLayer;
import org.matsim.population.Person;
import org.matsim.population.Population;
import org.xml.sax.helpers.AttributesImpl;

public abstract class LinkEvent extends BasicEvent {

	public String linkId;
	public transient Link link;
	/**
	 * The number of the leg in the plans file, starting from 0.
	 */
	public int legId;

	LinkEvent(final double time, final Person agent, final Link link, final int legNumber) {
		super(time, agent);
		this.legId = legNumber;
		this.link = link;
		this.linkId = link.getId().toString();
	}

	LinkEvent(final double time, final String agentId, final String linkId, final int legNumber) {
		super(time, agentId);
		this.legId = legNumber;
		this.linkId = linkId;
	}

	protected AttributesImpl getAttributesImpl() {
		AttributesImpl attr = new AttributesImpl();

		long lTime = (long)this.time; // TODO [DS] switch to double for times
		attr.addAttribute("","","time", "", Long.toString(lTime));
		attr.addAttribute("","","agent", "", this.agentId);
		attr.addAttribute("","","link", "", this.linkId);
		attr.addAttribute("","","leg", "", Integer.toString(this.legId));
		return attr;
	}

	protected String asString() {
		return getTimeString(this.time) + this.agentId + "\t" + this.legId + "\t" + this.linkId + "\t0\t"; // FLAG + DESCRIPTION is missing here: concat later
	}

	protected void rebuildLinkData(final Population population, final NetworkLayer network) {
		this.agent = population.getPerson(this.agentId);
		this.link = (Link)network.getLocation(this.linkId);
	}

}
