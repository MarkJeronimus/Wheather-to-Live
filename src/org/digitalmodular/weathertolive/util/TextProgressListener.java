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

import java.io.PrintStream;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2011-02-24
public class TextProgressListener implements ProgressListener {
	private final PrintStream writer;
	private final int         updateGranularity;

	private String lastPrinted  = "";
	private int    nextUpdateAt = 0;

	public TextProgressListener(PrintStream writer, int updateGranularity) {
		this.writer = requireNonNull(writer, "writer");
		this.updateGranularity = requireAtLeast(1, updateGranularity, "updateGranularity");
	}

	@Override
	public void progressUpdated(ProgressEvent evt) {
		boolean indeterminate = evt.getTotal() <= 0;
		long    progress      = evt.getProgress();
		boolean complete      = !indeterminate && progress >= evt.getTotal();

		long overshotBy = progress - nextUpdateAt;
		if (overshotBy < 0 && !complete) {
			return;
		}

		nextUpdateAt += (overshotBy / updateGranularity + 1) * updateGranularity;

		eraseLine();

		lastPrinted = indeterminate
		              ? Long.toString(progress) + ' ' + evt.getText()
		              : Long.toString(progress) + '/' + Long.toString(evt.getTotal()) + ' ' +
		                evt.getText();

		writer.print(lastPrinted);

		if (complete) {
			writer.println();
			lastPrinted = "";
			nextUpdateAt = 0;
		}
	}

	private void eraseLine() {
		writer.print("\b".repeat(lastPrinted.length()));
		lastPrinted = "";
	}
}
