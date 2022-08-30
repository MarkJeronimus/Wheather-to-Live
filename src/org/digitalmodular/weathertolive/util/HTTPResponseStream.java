/*
 * This file is part of Weather to Live.
 *
 * Copyleft 2022 Mark Jeronimus. All Rights Reversed.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllUtilities. If not, see <http://www.gnu.org/licenses/>.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.digitalmodular.weathertolive.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireRange;

/**
 * @author Mark Jeronimus
 */
// Created 2015-10-17
public class HTTPResponseStream extends InputStreamWithLength {
	private final Collection<ProgressListener> listeners = new CopyOnWriteArrayList<>();

	private final int                       responseCode;
	private final Map<String, List<String>> responseHeaders;

	private int position = 0;

	public HTTPResponseStream(URL url,
	                          InputStream in,
	                          int responseCode,
	                          Map<String, List<String>> responseHeaders,
	                          int length) {
		super(in, length);
		this.responseCode = requireRange(100, 599, responseCode, "responseCode");
		this.responseHeaders = requireNonNull(responseHeaders, "responseHeaders");
	}

	public int getResponseCode() {
		return responseCode;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public Map<String, List<String>> getResponseHeaders() {
		return responseHeaders;
	}

	public synchronized void addProgressListener(ProgressListener listener) {
		requireNonNull(listener, "listener");

		listeners.add(listener);
	}

	public synchronized void removeProgressListener(ProgressListener listener) {
		requireNonNull(listener, "listener");

		listeners.remove(listener);
	}

	@Override
	public synchronized int read() throws IOException {
		int read = super.read();

		if (read > 0) {
			position++;
			fireUpdated();
		}

		return read;
	}

	@Override
	public synchronized int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		int read = super.read(b, off, len);

		if (read > 0) {
			position += read;
			fireUpdated();
		}

		return read;
	}

	@Override
	public synchronized long skip(long n) throws IOException {
		long skipped = super.skip(n);

		position += skipped;
		fireUpdated();

		return skipped;
	}

	@Override
	public synchronized void close() throws IOException {
		super.close();

		int length = getLength();
		if (length == -1) {
			fireCompleted();
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	private void fireUpdated() {
		int length = getLength();
		if (length >= 0 && position == length) {
			fireCompleted();
			return;
		}

		String message = "Downloading... " + (int)(100.0f * position / length) + '%';

		ProgressEvent event = new ProgressEvent(this, position, length, message);
		fireProgressUpdated(event);
	}

	private void fireCompleted() {
		int length = getLength();
		length = length >= 0 ? length : position;
		String message = position == length ? "Download Complete" : "Download Aborted";

		ProgressEvent event = new ProgressEvent(this, position, length, message);
		fireProgressUpdated(event);
	}

	private void fireProgressUpdated(ProgressEvent event) {
		for (ProgressListener listener : listeners)
			listener.progressUpdated(event);
	}
}
