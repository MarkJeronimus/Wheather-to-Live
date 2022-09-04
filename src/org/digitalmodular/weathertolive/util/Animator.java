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

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.Timer;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-29
public class Animator {
	private final Consumer<BufferedImage> imageSink;

	private List<AnimationFrame> animation = Collections.emptyList();

	private final Timer timer;

	private int  animationFrame        = 0;
	private long nextAnimationStepTick = 0;

	private final Set<IntConsumer> animationListeners = new CopyOnWriteArraySet<>();

	public Animator(Consumer<BufferedImage> imageSink) {
		this.imageSink = requireNonNull(imageSink, "imageSink");

		timer = new Timer(1, this::animationStep);
		timer.setRepeats(true);
		timer.setCoalesce(true);
	}

	public @Nullable List<AnimationFrame> getAnimation() {
		//noinspection AssignmentOrReturnOfFieldWithMutableType // Suppress IntelliJ bug
		return animation;
	}

	public void setAnimation(List<AnimationFrame> animation) {
		requireNonNull(animation, "animation");

		boolean sameLength = this.animation.size() == animation.size();

		this.animation = Collections.unmodifiableList(animation);

		if (sameLength) {
			AnimationFrame frame = animation.get(animationFrame);

			imageSink.accept(frame.getImage());
		} else {
			stopAnimation();

			// display first frame
			setAnimationFrame(0);
		}
	}

	public void startAnimation() {
		if (animation.isEmpty()) {
			return;
		}

		nextAnimationStepTick = System.nanoTime();

		timer.start();
	}

	public void stopAnimation() {
		timer.stop();
	}

	public int getAnimationFrame() {
		return animationFrame;
	}

	public void setAnimationFrame(int animationFrame) {
		if (animation.isEmpty()) {
			return;
		}

		this.animationFrame = NumberUtilities.clamp(animationFrame, 0, animation.size() - 1);
		nextAnimationStepTick = System.nanoTime();
		switchFrame();
	}

	public void animationStep(ActionEvent ignored) {
		if (animation.isEmpty()) {
			return;
		}

		long now       = System.nanoTime();
		long remaining = nextAnimationStepTick - now;
		if (remaining < 0) {
			animationFrame = (animationFrame + 1) % animation.size();
			switchFrame();
		}
	}

	/**
	 * Displays the current frame and increments the next timestamp with the duration of the frame.
	 * <p>
	 * This assumes {@link #animationFrame} has already been incremented or overwritten.
	 */
	private void switchFrame() {
		assert !animation.isEmpty();

		AnimationFrame frame = animation.get(animationFrame);

		nextAnimationStepTick += frame.getDurationNanos();

		imageSink.accept(frame.getImage());
		fireAnimationListeners();
	}

	public void addAnimationListener(IntConsumer animationListener) {
		animationListeners.add(animationListener);
	}

	public void removeAnimationListener(IntConsumer animationListener) {
		animationListeners.remove(animationListener);
	}

	private void fireAnimationListeners() {
		@Nullable RuntimeException thrown = null;

		for (IntConsumer animationListener : animationListeners) {
			try {
				animationListener.accept(animationFrame);
			} catch (RuntimeException ex) {
				if (thrown == null) {
					thrown = ex;
				} else {
					thrown.addSuppressed(ex);
				}
			}
		}

		if (thrown != null) {
			//noinspection ProhibitedExceptionThrown
			throw thrown;
		}
	}
}
