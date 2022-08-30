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

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNullOrNotEmpty;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-29
public class AnimationZoomPanel extends ZoomPanel {
	private @Nullable List<AnimationFrame> animation = null;

	private final Timer timer;

	private int  animationFrame        = 0;
	private long nextAnimationStepTick = 0;

	public AnimationZoomPanel() {
		timer = new Timer(1, this::animationStep);
		timer.setRepeats(true);
		timer.setCoalesce(true);
	}

	public @Nullable List<AnimationFrame> getAnimation() {
		//noinspection AssignmentOrReturnOfFieldWithMutableType // Suppress IntelliJ bug
		return animation;
	}

	@Override
	public void setImage(@Nullable BufferedImage image) {
		setAnimation(null);
		super.setImage(image);
	}

	public void setAnimation(@Nullable List<AnimationFrame> animation) {
		stopAnimation();

		requireNullOrNotEmpty(animation, "animation");
		this.animation = animation == null ? null : Collections.unmodifiableList(animation);

		super.setImage(animation == null ? null : animation.get(0).getImage());
	}

	public void startAnimation() {
		if (animation == null) {
			return;
		}

		animationFrame = 0;
		nextAnimationStepTick = System.nanoTime();
		nextFrame();

		timer.start();
	}

	public void stopAnimation() {
		timer.stop();
	}

	public void animationStep(ActionEvent ignored) {
		if (animation == null) {
			return;
		}

		long now       = System.nanoTime();
		long remaining = nextAnimationStepTick - now;
		if (remaining < 0) {
			System.out.println(remaining);
			animationFrame = (animationFrame + 1) % animation.size();
			nextFrame();
		}
	}

	private void nextFrame() {
		assert animation != null;

		AnimationFrame frame = animation.get(animationFrame);

		nextAnimationStepTick += frame.getDurationNanos();

		super.setImage(frame.getImage());
	}
}
