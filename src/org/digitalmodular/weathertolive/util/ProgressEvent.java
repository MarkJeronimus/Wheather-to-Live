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
 * Weather to Live. If not, see <http://www.gnu.org/licenses/>.
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

import java.util.EventObject;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2015-08-15
public class ProgressEvent extends EventObject {
	private final long   progress;
	private final long   total;
	private final String text;

	public ProgressEvent(Object source, long progress, long total, String text) {
		super(source);
		this.text = requireNonNull(text, "text");

		if (total > 0) {
			requireAtLeast(0, progress, "progress");
		} else {
			requireAtLeast(-1, progress, "progress");
		}

		this.progress = progress;
		this.total = total;

	}

	public long getProgress() {
		return progress;
	}

	public long getTotal() {
		return total;
	}

	public String getText() {
		return text;
	}

	public boolean isComplete() {
		if (Thread.currentThread().isInterrupted()) {
			return false;
		}

		if (total > 0) {
			return progress >= total;
		} else {
			return progress >= 0;
		}
	}

	@Override
	public int hashCode() {
		int hash = 0x811C9DC5;
		hash ^= Long.hashCode(progress);
		hash *= 0x01000193;
		hash ^= Long.hashCode(total);
		hash *= 0x01000193;
		hash ^= text.hashCode();
		hash *= 0x01000193;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProgressEvent other = (ProgressEvent)obj;
		return progress == other.progress && total == other.total && text.equals(other.text);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(text.isEmpty() ? 64 : 128).append("ProgressEvent(");
		sb.append("progress=").append(progress);
		sb.append(", total=").append(total);
		sb.append(", source=").append(source);

		if (!text.isEmpty()) {
			sb.append(", text=").append(text);
		}

		return sb.append(')').toString();
	}
}
