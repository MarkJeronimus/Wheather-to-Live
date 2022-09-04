package org.digitalmodular.weathertolive.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	private final Lock      lock      = new ReentrantLock();
	private final Condition condition = lock.newCondition();

	private final AtomicBoolean taskRunning  = new AtomicBoolean();
	private final AtomicBoolean taskAborting = new AtomicBoolean();

	public ClimateDataSet load(ClimateDataSetMetadata metadata, MultiProgressListener progressListener)
			throws IOException, InterruptedException {
		long t = System.nanoTime();

		taskRunning.set(true);
		taskAborting.set(false);
		try {
			int                 numDataSets = metadata.getNumMetadata();
			List<FilterDataSet> dataSets    = new ArrayList<>(numDataSets);

			ProgressListener loadProgressListener = progressListener.wrapAsSingleProgressListener(1);

			for (int i = 0; i < numDataSets; i++) {
				ClimateDataSetData setMetadata = metadata.getMetadata(i);

				String filename = setMetadata.filename;

				progressListener.multiProgressUpdated(0, new ProgressEvent(
						metadata, i, numDataSets, filename));

				if (taskAborting.get()) {
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

			return new ClimateDataSet(metadata, dataSets);
		} finally {
			System.out.println("Loading took " + (System.nanoTime() - t) / 1.0e9f + " s");

			if (taskAborting.get()) {
				progressListener.multiProgressUpdated(0, new ProgressEvent(
						metadata, 1, 1, "Canceled"));
			}

			lock.lock();
			try {
				System.out.println("taskRunning.set(false);");
				taskRunning.set(false);
				condition.signalAll();
			} finally {
				lock.unlock();
			}
		}
	}

	public void cancel() {
		lock.lock();
		try {
			if (taskRunning.get()) {
				taskAborting.set(true);
				worldClim21DataSetLoader.cancel();
				crucl20DataSetLoader.cancel();

				try {
					while (taskRunning.get()) {
						condition.await(1, TimeUnit.MILLISECONDS);
					}
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}
		} finally {
			lock.unlock();
		}
	}
}
