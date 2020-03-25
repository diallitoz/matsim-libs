
/* *********************************************************************** *
 * project: org.matsim.*
 * AbstractQSimModuleTest.java
 *                                                                         *
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
 * *********************************************************************** */

 package org.matsim.core.mobsim.qsim;

import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.IterationScoped;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.agents.AgentFactory;
import org.matsim.core.mobsim.qsim.agents.DefaultAgentFactory;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.concurrent.atomic.AtomicLong;

public class AbstractQSimModuleTest {	

	@Test
	public void testOverrideAgentFactory() {
		Config config = ConfigUtils.createConfig();
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(0);

		Scenario scenario = ScenarioUtils.loadScenario(config);

		Person person = scenario.getPopulation().getFactory().createPerson(Id.createPersonId("person"));
		person.addPlan(scenario.getPopulation().getFactory().createPlan());
		scenario.getPopulation().addPerson(person);

		AtomicLong value = new AtomicLong(0);

		Controler controler = new Controler(scenario);
		controler.addOverridingQSimModule(new TestQSimModule(value));
		controler.run();

		Assert.assertTrue(value.get() > 0);
	}

	private class TestQSimModule extends AbstractQSimModule {
		private final AtomicLong value;

		public TestQSimModule(AtomicLong value) {
			this.value = value;
		}

		@Override
		protected void configureQSim() {
			bind(AtomicLong.class).toInstance(value);
			bind(AgentFactory.class).to(TestAgentFactory.class).in(IterationScoped.class);
		}
	}

	static private class TestAgentFactory implements AgentFactory {
		private final AgentFactory delegate;
		private final AtomicLong value;

		@Inject
		public TestAgentFactory(Netsim simulation, AtomicLong value) {
			delegate = new DefaultAgentFactory(simulation);
			this.value = value;
		}

		@Override
		public MobsimAgent createMobsimAgentFromPerson(Person p) {
			value.incrementAndGet();
			return delegate.createMobsimAgentFromPerson(p);
		}
	}

	@Test
	public void testAddEngine() {
		Config config = ConfigUtils.createConfig();
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(0);

		Scenario scenario = ScenarioUtils.loadScenario(config);

		TestEngine engine = new TestEngine();

		Controler controler = new Controler(scenario);

		controler.addOverridingQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				addQSimComponentBinding("MyEngine").toInstance(engine);
			}
		});

		controler.configureQSimComponents(components -> {
			components.addNamedComponent("MyEngine");
		});

		controler.run();

		Assert.assertTrue(engine.called);
	}

	static private class TestEngine implements MobsimEngine {
		public boolean called = false;

		@Override
		public void doSimStep(double time) {
			called = true;
		}

		@Override
		public void onPrepareSim() {
		}

		@Override
		public void afterSim() {
		}

		@Override
		public void setInternalInterface(InternalInterface internalInterface) {
		}
	}
}
