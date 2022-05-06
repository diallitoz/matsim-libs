package org.matsim.contrib.av.robotaxi.run;

import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.taxi.run.MultiModeTaxiConfigGroup;
import org.matsim.contrib.taxi.run.TaxiControlerCreator;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import java.net.URL;

/**
 * @author aodiallo
 *
 */

public class RunPAV {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//The config_file of scenario 1
		String config_file = "C:\\Users\\aziseoumar.diallo\\Documents\\Simulation\\Input\\Robotaxi\\input\\scenario1\\config_sce1.xml";
		Config config = ConfigUtils.loadConfig( config_file, new ConfigGroup[]{new DvrpConfigGroup(), new OTFVisConfigGroup(), new MultiModeTaxiConfigGroup()});
		TaxiControlerCreator.createControler(config, false).run();
	}


}
