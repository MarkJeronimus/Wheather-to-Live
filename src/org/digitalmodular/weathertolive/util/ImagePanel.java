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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jetbrains.annotations.Nullable;

/**
 * @author Mark Jeronimus
 */
// Created 2011-06-20
public class ImagePanel extends JPanel {
	private @Nullable Image   image;
	private           boolean stretch;

	public ImagePanel() {
		this(null);
	}

	public ImagePanel(@Nullable Image image) {
		this(image, false);
	}

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public ImagePanel(@Nullable Image image, boolean stretch) {
		super(null);
		setOpaque(false);

		this.image = image;
		this.stretch = stretch;

		pack();
	}

	public @Nullable Image getImage() {
		return image;
	}

	public void setImage(@Nullable Image image) {
		this.image = image;
		repaint();
	}

	public boolean isStretch() {
		return stretch;
	}

	public void setStretch(boolean stretch) {
		this.stretch = stretch;

		repaint();
	}

	public void pack() {
		if (stretch) {
			repaint();
			return;
		}

		Dimension size = new Dimension();

		if (image != null) {
			size.width = image.getWidth(null);
			size.height = image.getHeight(null);
		}

		Border border = getBorder();
		if (border != null) {
			Insets insets = border.getBorderInsets(this);

			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;
		}

		setPreferredSize(size);
		setSize(size);
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (image == null) {
			return;
		}

		int x      = 0;
		int y      = 0;
		int width  = getWidth();
		int height = getHeight();

		Border border = getBorder();
		if (border != null) {
			Insets insets = border.getBorderInsets(this);

			x = insets.left;
			y = insets.top;
			width -= insets.right + x;
			height -= insets.right + x;
		}

		if (stretch) {
			g.drawImage(image, x, y, width, height, this);
		} else {
			g.drawImage(image, x, y, null);
		}
	}
}
