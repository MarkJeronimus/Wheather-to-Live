package org.digitalmodular.weathertolive.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.digitalmodular.weathertolive.util.MultiProgressListener;
import org.digitalmodular.weathertolive.util.ProgressEvent;
import org.digitalmodular.weathertolive.util.ProgressListener;
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
		int                 numDataSets = metadata.getNumMetadata();
		List<FilterDataSet> dataSets    = new ArrayList<>(numDataSets);
		long                t           = System.nanoTime();

		ProgressListener loadProgressListener = progressListener.wrapAsSingleProgressListener(1);

		for (int i = 0; i < numDataSets; i++) {
			ClimateDataSetData setMetadata = metadata.getMetadata(i);

			String filename = setMetadata.filename;

			progressListener.multiProgressUpdated(0, new ProgressEvent(
					metadata, i, numDataSets, filename));

			if (filename.endsWith(".zip")) {
				dataSets.add(WorldClim21DataSetFactory.createFor(setMetadata, loadProgressListener));
			} else if (filename.endsWith(".gz")) {
				dataSets.add(CRUCL20DataSetFactory.createFor(setMetadata, loadProgressListener));
			} else {
				throw new IllegalStateException("Unknown dataset data file: " + filename);
			}
		}

		progressListener.multiProgressUpdated(0, new ProgressEvent(
				metadata, numDataSets, numDataSets, ""));

		System.out.println("Loading took " + (System.nanoTime() - t) / 1.0e9f + " s");
		return new ClimateDataSet(dataSets);
	}
}
