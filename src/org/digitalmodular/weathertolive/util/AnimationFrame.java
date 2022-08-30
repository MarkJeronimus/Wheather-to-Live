package org.digitalmodular.weathertolive.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import static java.util.Objects.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2017-07-18
public class AnimationFrame {
	private final BufferedImage image;
	private final long          durationNanos;

	public AnimationFrame(BufferedImage image, long durationNanos) {
		this.image = requireNonNull(image);
		this.durationNanos = durationNanos;

		if (durationNanos < 1) {
			throw new IllegalArgumentException("'durationNanos' must be at least 1: " + durationNanos);
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	public long getDurationNanos() {
		return durationNanos;
	}

	public Dimension getSize() {
		return new Dimension(image.getWidth(), image.getHeight());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof AnimationFrame)) {
			return false;
		}

		AnimationFrame other = (AnimationFrame)o;
		return getDurationNanos() == other.getDurationNanos() &&
		       getImage().equals(other.getImage());
	}

	@Override
	public int hashCode() {
		int hashCode = 0x811C9DC5;
		hashCode = 0x01000193 * (hashCode ^ image.hashCode());
		hashCode = 0x01000193 * (hashCode ^ Long.hashCode(durationNanos));
		return hashCode;
	}
}
