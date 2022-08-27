/*
 * This file is part of AllUtilities.
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.WARNING;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * Wrapper for non-persistent http connections.
 * <ul><li>Protocols: http, https,</li>
 * <li>Methods: GET, POST,</li>
 * <li>Cookies,</li>
 * <li>User agent spoofing,</li>
 * <li>Compressions: gzip, deflate,</li>
 * <li>Custom header fields: yes (but not removal of existing header fields),</li>
 * <li>Timeout (default 10s),</li>
 * <li>Referer spoofing</li>
 * <li>Automatic HTTP/302 redirect (recursive, without infinite loop check)</li>
 * <li>Progress listeners,</li>
 * <li>Immutable header fields include {@code Accept} and {@code Accept-Language}.</li></ul>
 *
 * @author Mark Jeronimus
 */
// Created 2015-10-17
public class HTTPDownloader {
	public static final String DEFAULT_USER_AGENT =
			"Mozilla/5.0 (compatible; Java/" + System.getProperty("java.version") + ')';
	public static final int    DEFAULT_TIMEOUT    = 10_000;

	private       String                       userAgent         = DEFAULT_USER_AGENT;
	private final Map<String, String>          cookies           = new LinkedHashMap<>(16);
	private       boolean                      doReferer         = true;
	private       int                          timeout           = DEFAULT_TIMEOUT;
	private final Collection<ProgressListener> progressListeners = new CopyOnWriteArraySet<>();

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = requireNonNull(userAgent, "userAgent");
	}

	public final void setCookie(String cookieName, @Nullable String cookieValue) {
		requireNonNull(cookieName, "cookieName");

		if (cookieValue == null) {
			cookies.remove(cookieName);
		} else {
			cookies.put(cookieName, cookieValue);
		}
	}

	public Map<String, String> getCookies() {
		return Collections.unmodifiableMap(cookies);
	}

	public boolean isDoReferer() {
		return doReferer;
	}

	public void setDoReferer(boolean doReferer) {
		this.doReferer = doReferer;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void addProgressListener(ProgressListener progressListener) {
		progressListeners.add(progressListener);
	}

	public void removeProgressListener(ProgressListener progressListener) {
		progressListeners.remove(progressListener);
	}

	/**
	 * Requests a URL and if successful (response 200), returns the open stream. Closing this stream closes the
	 * connection.
	 */
	public HTTPResponseStream openConnection(URL url,
	                                         byte @Nullable [] postData,
	                                         String... extraRequestProperties) throws IOException {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection)url.openConnection();
			connection.addRequestProperty("User-Agent", userAgent);
			connection.addRequestProperty("Accept", "*.*");
			connection.addRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.addRequestProperty("Accept-Encoding", "gzip, deflate");
			addExtraRequestProperty(connection, getCookieString(cookies));
			addExtraRequestProperties(connection, extraRequestProperties);
			connection.addRequestProperty("Connection", "keep-alive");
			connection.addRequestProperty("DNT", "1");
			addPostDataHeader(postData, connection);

			if (doReferer) {
				connection.addRequestProperty("Referer", url.toString() + '/');
			}

			connection.setInstanceFollowRedirects(false);

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			addPostData(postData, connection);

			int responseCode = connection.getResponseCode();

			InputStream in;
			try {
				in = connection.getInputStream();
			} catch (IOException ex) {
				in = connection.getErrorStream();
				if (in == null) {
					throw ex;
				}
			}

			in = getDecompressedStream(connection, in);

			int                       contentLength   = attemptGetStreamLength(connection);
			Map<String, List<String>> responseHeaders = connection.getHeaderFields();

			HTTPResponseStream stream = new HTTPResponseStream(url, in, responseCode, responseHeaders, contentLength);
			progressListeners.forEach(stream::addProgressListener);

			return stream;
		} catch (IOException e) {
			if (connection != null) {
				connection.disconnect();
			}

			throw e;
		}
	}

	public void downloadToFile(URL url, byte @Nullable [] postData, Path file, String... extraRequestProperties)
			throws IOException {
		try (HTTPResponseStream stream = openConnection(url, postData, extraRequestProperties);
		     OutputStream out = Files.newOutputStream(file)) {
			if (stream.getResponseCode() == 302) {
				List<String> locationHeaderResponse = stream.getResponseHeaders().get("Location");
				if (locationHeaderResponse == null) {
					throw new IOException(
							"\"HTTP/302 Found\" without \"Location\" response header: " + stream.getResponseHeaders());
				}

				String newLocation = locationHeaderResponse.get(0);
				URL    redirect    = new URL(url, newLocation);

				if (Logger.getGlobal().isLoggable(FINER)) {
					Logger.getGlobal().log(FINER, "Redirecting to: " + redirect);
				}

				downloadToFile(redirect, null, file, extraRequestProperties);
				return;
			}

			if (stream.getResponseCode() / 100 != 2) {
				throw new IOException("Received " + stream.getResponseHeaders().get(null) + " for " + url);
			}

			stream.transferTo(out);
		} catch (IOException ex) {
			if (Files.exists(file)) {
				Logger.getGlobal().log(WARNING, "Removing partially downloaded file because of exception");
				Files.delete(file);
			}

			throw ex;
		}
	}

	private static void addExtraRequestProperties(URLConnection connection, String[] extraRequestProperties) {
		for (String extraRequestProperty : extraRequestProperties)
			addExtraRequestProperty(connection, extraRequestProperty);
	}

	private static void addExtraRequestProperty(URLConnection connection, @Nullable String s) {
		if (s == null) {
			return;
		}

		int i = s.indexOf(": ");
		if (i == -1) {
			throw new IllegalArgumentException("Not a request property (must contain a \": \"): " + s);
		}

		connection.addRequestProperty(s.substring(0, i), s.substring(i + 2));
	}

	private static @Nullable String getCookieString(Map<String, String> cookies) {
		if (cookies.isEmpty()) {
			return null;
		}

		StringBuilder cookieProperty = new StringBuilder(256);
		for (Entry<String, String> cookie : cookies.entrySet()) {
			if (cookieProperty.length() == 0) {
				cookieProperty.append("Cookie:");
			} else {
				cookieProperty.append(';');
			}

			cookieProperty.append(' ');
			cookieProperty.append(cookie.getKey());
			cookieProperty.append('=');
			cookieProperty.append(cookie.getValue());
		}
		return cookieProperty.toString();
	}

	private static void addPostDataHeader(byte @Nullable [] postData, HttpURLConnection connection)
			throws ProtocolException {
		if (postData != null) {
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.addRequestProperty("Content-length", Integer.toString(postData.length));
		}
	}

	@SuppressWarnings("TypeMayBeWeakened")
	private static void addPostData(byte @Nullable [] postData, HttpURLConnection connection) throws IOException {
		if (postData != null) {
			connection.setDoOutput(true);
			try (OutputStream os = connection.getOutputStream()) {
				os.write(postData);
			}
		}
	}

	@SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
	private static InputStream getDecompressedStream(URLConnection connection, InputStream in) throws IOException {
		if ("gzip".equals(connection.getHeaderField("Content-Encoding"))) {
			in = new GZIPInputStream(in);
		} else if ("deflate".equals(connection.getHeaderField("Content-Encoding"))) {
			in = new DeflaterInputStream(in);
		}
		return in;
	}

	private static int attemptGetStreamLength(URLConnection connection) {
		try {
			return Integer.parseInt(connection.getHeaderField("Content-Length"));
		} catch (NumberFormatException ignored) {
			return -1;
		}
	}
}
