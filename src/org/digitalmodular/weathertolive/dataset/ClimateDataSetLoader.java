package org.digitalmodular.weathertolive.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.digitalmodular.weathertolive.util.MultiProgressListener;
import org.digitalmodular.weathertolive.util.ProgressEvent;
import org.digitalmodular.weathertolive.util.ProgressListener;
import static org.digitalmodular.weathertolive.dataset.ClimateDataSetMetadata.ClimateDataSetData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-03
public final class ClimateDataSetLoader {
	private final WorldClim21DataSetLoader worldClim21DataSetLoader = new WorldClim21DataSetLoader();
	private final CRUCL20DataSetLoader     crucl20DataSetLoader     = new CRUCL20DataSetLoader();

	private final AtomicBoolean cancelRequested = new AtomicBoolean();

	public ClimateDataSet load(ClimateDataSetMetadata metadata, MultiProgressListener progressListener)
			throws IOException, InterruptedException {
		cancelRequested.set(false);

		int                 numDataSets = metadata.getNumMetadata();
		List<FilterDataSet> dataSets    = new ArrayList<>(numDataSets);
		long                t           = System.nanoTime();

		ProgressListener loadProgressListener = progressListener.wrapAsSingleProgressListener(1);

		for (int i = 0; i < numDataSets; i++) {
			ClimateDataSetData setMetadata = metadata.getMetadata(i);

			String filename = setMetadata.filename;

			progressListener.multiProgressUpdated(0, new ProgressEvent(
					metadata, i, numDataSets, filename));

			if (cancelRequested.get()) {
				throw new InterruptedException("Canceled");
			}

			if (filename.endsWith(".zip")) {
				dataSets.add(worldClim21DataSetLoader.load(setMetadata, loadProgressListener));
			} else if (filename.endsWith(".gz")) {
				dataSets.add(crucl20DataSetLoader.load(setMetadata, loadProgressListener));
			} else {
				throw new IllegalStateException("Unknown dataset data file: " + filename);
			}
		}

		progressListener.multiProgressUpdated(0, new ProgressEvent(
				metadata, numDataSets, numDataSets, ""));

		System.out.println("Loading took " + (System.nanoTime() - t) / 1.0e9f + " s");
		return new ClimateDataSet(dataSets);
	}

	public void cancel() {
		cancelRequested.set(true);
		worldClim21DataSetLoader.cancel();
		crucl20DataSetLoader.cancel();
	}
}
