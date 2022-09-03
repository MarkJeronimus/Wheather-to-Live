package org.digitalmodular.weathertolive.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.digitalmodular.weathertolive.util.MultiProgressListener;
import static org.digitalmodular.weathertolive.dataset.ClimateDataSetMetadata.ClimateDataSetData;

/**
 * @author author
 */
// Created 2022-09-03
public final class ClimateDataSetLoader {
	private ClimateDataSetLoader() {
		throw new AssertionError();
	}

	public static ClimateDataSet load(ClimateDataSetMetadata metadata, MultiProgressListener progressListener)
			throws IOException {
		List<FilterDataSet> dataSets = new ArrayList<>(metadata.getNumMetadata());
		long                t        = System.nanoTime();

		for (int i = 0; i < metadata.getNumMetadata(); i++) {
			ClimateDataSetData setMetadata = metadata.getMetadata(i);

			String filename = setMetadata.filename;

			if (filename.endsWith(".zip")) {
				dataSets.add(WorldClim21DataSetFactory.createFor(setMetadata));
			} else if (filename.endsWith(".gz")) {
				dataSets.add(CRUCL20DataSetFactory.createFor(setMetadata));
			} else {
				throw new IllegalStateException("Unknown dataset data file: " + filename);
			}
		}

		System.out.println("Loading took " + (System.nanoTime() - t) / 1.0e9f + " s");
		return new ClimateDataSet(dataSets);
	}
}
