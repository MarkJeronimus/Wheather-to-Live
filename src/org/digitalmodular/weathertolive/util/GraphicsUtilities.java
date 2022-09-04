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

import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_COLOR_RENDERING;
import static java.awt.RenderingHints.KEY_DITHERING;
import static java.awt.RenderingHints.KEY_FRACTIONALMETRICS;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_STROKE_CONTROL;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_DITHER_DISABLE;
import static java.awt.RenderingHints.VALUE_DITHER_ENABLE;
import static java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
import static java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_STROKE_DEFAULT;
import static java.awt.RenderingHints.VALUE_STROKE_PURE;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.Nullable;

/**
 * @author Mark Jeronimus
 */
// Created 2017-01-03
public final class GraphicsUtilities {
	private GraphicsUtilities() {
		throw new AssertionError();
	}

	/**
	 * Enables or disables high quality rendering on a graphics context. Along with anti-aliasing, some other
	 * properties like fractional metrics, offset and interpolation are enabled.
	 * <p>
	 * The coordinate system is also offset by {@code (0.5, 0.5)} so integer coordinates become pixel centers rather
	 * than upper-left corners (makes it easier to draw nice 1px wide strokes).
	 *
	 * @param g           the {@link Graphics2D} context
	 * @param antialiased to enable or disable it
	 */
	public static void setAntialiased(Graphics2D g, boolean antialiased) {
		if (antialiased) {
			if (!isAntialiased(g)) {
				g.translate(0.5, 0.5);
			}

			g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
			g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
			g.setRenderingHint(KEY_DITHERING, VALUE_DITHER_ENABLE);
			g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON);
			g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
			g.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
			g.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
		} else {
			if (isAntialiased(g)) {
				g.translate(-0.5, -0.5);
			}

			g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
			g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
			g.setRenderingHint(KEY_DITHERING, VALUE_DITHER_DISABLE);
			g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF);
			g.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_OFF);
			g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_SPEED);
			g.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_SPEED);
			g.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_DEFAULT);
		}
	}

	@SuppressWarnings("ObjectEquality") // 'Enum' objects
	public static boolean isAntialiased(Graphics2D g) {
		return g.getRenderingHint(KEY_ANTIALIASING) == VALUE_ANTIALIAS_ON;
	}

	/**
	 * Shorthand to get the default screen object.
	 */
	public static GraphicsDevice getDisplayDevice() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}

	/**
	 * Shorthand to get the display mode of the default screen.
	 */
	public static DisplayMode getDisplayMode() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
	}

	/**
	 * Get a valid display mode.
	 * <p>
	 *
	 * @return the display mode, or null if none matching the parameters was
	 * found.
	 */
	public static DisplayMode getDisplayMode(int width, int height) {
		GraphicsDevice displayDevice = getDisplayDevice();
		DisplayMode    currentMode   = displayDevice.getDisplayMode();
		DisplayMode[]  modes         = displayDevice.getDisplayModes();

		int bitDepth    = currentMode.getBitDepth();
		int refreshRate = currentMode.getRefreshRate();

		for (DisplayMode mode : modes) {
			//noinspection OverlyComplexBooleanExpression // Suppress IntelliJ bug
			if (mode.getHeight() == height &&
			    mode.getWidth() == width &&
			    mode.getBitDepth() == bitDepth &&
			    mode.getRefreshRate() == refreshRate) {
				return mode;
			}
		}

		refreshRate = Integer.MAX_VALUE;
		int index = -1;
		for (int i = modes.length - 1; i >= 0; i--) {
			DisplayMode mode = modes[i];
			if (mode.getHeight() == height &&
			    mode.getWidth() == width &&
			    mode.getBitDepth() == bitDepth) {
				if (refreshRate > mode.getRefreshRate()) {
					refreshRate = mode.getRefreshRate();
					index = i;
				}
			}
		}

		return index < 0 ? null : modes[index];
	}

	/**
	 * Shorthand to make a frame full-screen on the default screen.
	 */
	public static void setFullScreenWindow(JFrame frame) {
		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
	}

	public static void requireOnEDT() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new AssertionError("May only be called from the EDT");
		}
	}

	public static void requireNotOnEDT() {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new AssertionError("May not be called from the EDT");
		}
	}

	/**
	 * Executes the specified {@link Runnable} on the EDT thread. If the calling thread is the EDT thread, the
	 * specified {@code runnable} is invoked directly, otherwise it's placed on Swing's event dispatch queue and the
	 * method returns immediately.
	 * <p>
	 * Like with any callback, be sure not to throw unchecked exceptions,
	 * because these will crash the EDT thread, and with it, the application.
	 */
	public static void runOnEDT(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}

	/**
	 * Executes the specified {@link Callable} on the EDT thread. If the calling thread is the EDT thread, the
	 * specified {@code callable} is invoked directly, otherwise it's placed on Swing's event dispatch queue and the
	 * method waits for the result.
	 *
	 * @param <V>      the result type of the {@code callable}
	 * @param callable the callable task
	 * @return computed result
	 * @throws InterruptedException if we're interrupted while waiting for the
	 *                              event dispatching thread to finish executing doRun.run()
	 * @throws ExecutionException   if the computation threw an exception
	 */
	public static @Nullable <V> V getFromEDT(Callable<V> callable) throws InterruptedException, ExecutionException {
		RunnableFuture<V> f = new FutureTask<>(callable);

		if (SwingUtilities.isEventDispatchThread()) {
			f.run();
		} else {
			SwingUtilities.invokeLater(f);
		}

		return f.get();
	}
}
