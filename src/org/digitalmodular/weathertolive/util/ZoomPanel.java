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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireThat;

/**
 * @author Mark Jeronimus
 */
// Created 2014-05-30
public class ZoomPanel extends JPanel implements MouseListener,
                                                 MouseMotionListener,
                                                 MouseWheelListener,
                                                 KeyListener,
                                                 ComponentListener {
	private @Nullable BufferedImage image = null;

	private int minZoom = -16;
	private int maxZoom = 16;
	private int zoom    = 0;

	private boolean centered                 = true;
	private int     offsetX                  = 0;
	private int     offsetY                  = 0;
	private int     remainingAnimationFrames = 0;
	private int     newCenterX               = 0;
	private int     newCenterY               = 0;

	private int mouseX;
	private int mouseY;

	private @Nullable MouseAdapter imageListener = null;

	public ZoomPanel() {
		super(null);
		setPreferredSize(new Dimension(640, 640));
		setOpaque(true);

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		addComponentListener(this);
	}

	public @Nullable BufferedImage getImage() {
		return image;
	}

	public void setImage(@Nullable BufferedImage image) {
		if (Objects.equals(this.image, image))
			return;

		this.image = image;

		if (image != null)
			setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

		resetZoom();
		repaint();
	}

	public int getMinZoom() {
		return minZoom;
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public void setZoomLimits(int minZoom, int maxZoom) {
		requireThat(minZoom <= maxZoom, minZoom + " < " + maxZoom);

		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
	}

	public int getZoom() {
		return zoom;
	}

	/*
	 * The allowed range to zoom the image. Negative values are zoomed out, positive zoom in. Each unit further from
	 * zero is the next integer zoom ratio: 0 = 1:1, 1 = 2:1, 2 = 3:1, -1 = 1:2, etc
	 */
	public void setZoom(int zoom) {
		this.zoom = NumberUtilities.clamp(zoom, minZoom, maxZoom);
	}

	public void resetZoom() {
		stopAnimation();
		offsetX = 0;
		offsetY = 0;
		zoom = 0;

		if (image == null)
			return;

		// Find ideal zoom.
		int width       = Math.max(1, getWidth());
		int height      = Math.max(1, getHeight());
		int imageWidth  = image.getWidth();
		int imageHeight = image.getHeight();
		if (width > imageWidth)
			zoom = width / imageWidth - 1;
		else
			zoom = -(imageWidth / width);

		if (height > imageHeight)
			zoom = Math.min(zoom, height / imageHeight - 1);
		else
			zoom = Math.min(zoom, -(imageHeight / height));

		zoom = NumberUtilities.clamp(zoom, minZoom, maxZoom);

		centered = true;

		repaint();
	}

	public void setCenter(int x, int y) {
		if (image == null)
			return;

		centered = false;

		int imageWidth  = image.getWidth();
		int imageHeight = image.getHeight();

		int displayWidth  = multiplyByZoom(imageWidth);
		int displayHeight = multiplyByZoom(imageHeight);

		offsetX = displayWidth / 2 - multiplyByZoom(x);
		offsetY = displayHeight / 2 - multiplyByZoom(y);

		repaint();
	}

	public void startAnimation(int x, int y, int numSteps) {
		if (image == null)
			return;

		centered = false;

		int imageWidth  = image.getWidth();
		int imageHeight = image.getHeight();

		int displayWidth  = multiplyByZoom(imageWidth);
		int displayHeight = multiplyByZoom(imageHeight);

		newCenterX = displayWidth / 2 - multiplyByZoom(x);
		newCenterY = displayHeight / 2 - multiplyByZoom(y);

		remainingAnimationFrames = numSteps;
	}

	public void animate() {
		if (remainingAnimationFrames <= 0)
			return;

		int dx = newCenterX - offsetX;
		int dy = newCenterY - offsetY;
		offsetX += (int)(dx / (double)remainingAnimationFrames);
		offsetY += (int)(dy / (double)remainingAnimationFrames);
		remainingAnimationFrames--;

		repaint();
	}

	public void stopAnimation() {
		remainingAnimationFrames = 0;
	}

	public int multiplyByZoom(int imageWidth) {
		return multiplyByZoom(imageWidth, this.zoom);
	}

	private static int multiplyByZoom(int value, int zoom) {
		// zoom>0 - (1+zoom):1
		// zoom=0 - 1:1
		// zoom<0 - 1:(1-zoom)
		if (zoom > 0)
			return value * (1 + zoom);
		else if (zoom < 0)
			return value / (1 - zoom);

		return value;
	}

	public int divideByZoom(int value) {
		// zoom>0 - (1+zoom):1
		// zoom=0 - 1:1
		// zoom<0 - 1:(1-zoom)
		if (this.zoom > 0)
			return value / (1 + this.zoom);
		else if (this.zoom < 0)
			return value * (1 - this.zoom);

		return value;
	}

	public void setImageListener(@Nullable MouseAdapter imageListener) {
		this.imageListener = imageListener;
	}

	@Override
	public void paintComponent(Graphics g) {
		// Draw background.
		super.paintComponent(g);

		if (image == null)
			return;

		int imageWidth  = image.getWidth();
		int imageHeight = image.getHeight();

		int displayWidth  = multiplyByZoom(imageWidth);
		int displayHeight = multiplyByZoom(imageHeight);

		int x = (getWidth() - displayWidth) / 2 + offsetX;
		int y = (getHeight() - displayHeight) / 2 + offsetY;

		if (zoom < 0) {
			g.drawImage(image, x, y, displayWidth, displayHeight, this);
		} else {
			Point ul        = toImageCoordinate(0, 0);
			int   blockSize = multiplyByZoom(1);
			x += ul.x * blockSize;
			y += ul.y * blockSize;

			int visibleImageWidth  = (getWidth() + blockSize) / blockSize;
			int visibleImageHeight = (getHeight() + blockSize) / blockSize;

			g.drawImage(image,
			            x,
			            y,
			            x + visibleImageWidth * blockSize,
			            y + visibleImageHeight * blockSize,
			            ul.x,
			            ul.y,
			            ul.x + visibleImageWidth,
			            ul.y + visibleImageHeight,
			            this);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		requestFocus();

		if (image == null)
			return;

		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			mouseX = e.getX();
			mouseY = e.getY();
		}

		if (imageListener != null) {
			Point p = toImageCoordinate(e.getX(), e.getY());
			if (insideImage(p.x, p.y))
				imageListener.mousePressed(new MouseEvent(
						(Component)e.getSource(),
						e.getID(),
						e.getWhen(),
						e.getModifiers() | e.getModifiersEx(),
						p.x,
						p.y,
						e.getClickCount(),
						e.isPopupTrigger(),
						e.getButton()));
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (image == null)
			return;

		if (imageListener != null) {
			Point p = toImageCoordinate(e.getX(), e.getY());
			if (insideImage(p.x, p.y))
				imageListener.mouseReleased(new MouseEvent(
						(Component)e.getSource(),
						e.getID(),
						e.getWhen(),
						e.getModifiers() | e.getModifiersEx(),
						p.x,
						p.y,
						e.getClickCount(),
						e.isPopupTrigger(),
						e.getButton()));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (image == null)
			return;

		if (imageListener != null) {
			Point p = toImageCoordinate(e.getX(), e.getY());
			if (insideImage(p.x, p.y))
				imageListener.mouseClicked(new MouseEvent(
						(Component)e.getSource(),
						e.getID(),
						e.getWhen(),
						e.getModifiers() | e.getModifiersEx(),
						p.x,
						p.y,
						e.getClickCount(),
						e.isPopupTrigger(),
						e.getButton()));

		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (image == null)
			return;

		if (imageListener != null) {
			Point p = toImageCoordinate(e.getX(), e.getY());
			if (insideImage(p.x, p.y))
				imageListener.mouseMoved(new MouseEvent(
						(Component)e.getSource(),
						e.getID(),
						e.getWhen(),
						e.getModifiers() | e.getModifiersEx(),
						p.x,
						p.y,
						e.getClickCount(),
						e.isPopupTrigger()));
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (image == null)
			return;

		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			offsetX += e.getX() - mouseX;
			offsetY += e.getY() - mouseY;

			mouseX = e.getX();
			mouseY = e.getY();

			centered = false;
			repaint();
		}

		if (imageListener != null) {
			Point p = toImageCoordinate(e.getX(), e.getY());
			if (insideImage(p.x, p.y))
				imageListener.mouseDragged(new MouseEvent((Component)e.getSource(),
				                                          e.getID(),
				                                          e.getWhen(),
				                                          e.getModifiers() | e.getModifiersEx(),
				                                          p.x,
				                                          p.y,
				                                          e.getClickCount(),
				                                          e.isPopupTrigger()));
		}
	}

	private Point toImageCoordinate(int x, int y) {
		assert image != null;

		int imageWidth  = image.getWidth();
		int imageHeight = image.getHeight();

		int displayWidth  = multiplyByZoom(imageWidth);
		int displayHeight = multiplyByZoom(imageHeight);

		x -= (getWidth() - displayWidth) / 2 + offsetX;
		y -= (getHeight() - displayHeight) / 2 + offsetY;

		x = divideByZoom(x);
		y = divideByZoom(y);

		return new Point(x, y);
	}

	private boolean insideImage(int x, int y) {
		assert image != null;

		int imageWidth  = image.getWidth();
		int imageHeight = image.getHeight();
		return x >= 0 && y >= 0 && x < imageWidth && y < imageHeight;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (image == null)
			return;

		int mouseX        = e.getX() - getWidth() / 2;
		int mouseY        = e.getY() - getHeight() / 2;
		int wheelRotation = -e.getWheelRotation();

		int oldZoom = zoom;

		if (wheelRotation > 0) {
			if (zoom >= 5)
				wheelRotation *= 2;
		} else {
			if (zoom >= 7)
				wheelRotation *= 2;
		}

		zoom += wheelRotation;
		zoom = NumberUtilities.clamp(zoom, minZoom, maxZoom);
		System.out.println("zoom: " + zoom);

		// o -= mouse // move: mouse coordinate to origin
		// o *= zoomFactor / oldZoomFactor // scale: by zoom factor ratio
		// o += mouse // move: origin back to mouse position
		// Because of integer arithmetic: Grow before shrinking.
		if (-oldZoom > zoom) {
			stopAnimation();
			offsetX = multiplyByZoom(multiplyByZoom(offsetX - mouseX, -oldZoom)) + mouseX;
			offsetY = multiplyByZoom(multiplyByZoom(offsetY - mouseY, -oldZoom)) + mouseY;
		} else {
			stopAnimation();
			offsetX = multiplyByZoom(multiplyByZoom(offsetX - mouseX), -oldZoom) + mouseX;
			offsetY = multiplyByZoom(multiplyByZoom(offsetY - mouseY), -oldZoom) + mouseY;
		}

		centered = false;
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (image == null)
			return;

		if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			// Toggle between fit and 1:1
			if (!centered) {
				resetZoom();
			} else {
				resetZoom();
				centered = zoom == 0;
				zoom = 0;
			}
			repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (image == null || !centered)
			return;

		repaint();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
}
