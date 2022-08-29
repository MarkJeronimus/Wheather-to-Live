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
	private final int           durationMillis;

	public AnimationFrame(BufferedImage image, int durationMillis) {
		this.image = requireNonNull(image);
		this.durationMillis = durationMillis;

		if (durationMillis < 1) {
			throw new IllegalArgumentException("'durationMillis' must be at least 1: " + durationMillis);
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getDurationMillis() {
		return durationMillis;
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
		return getDurationMillis() == other.getDurationMillis() &&
		       getImage().equals(other.getImage());
	}

	@Override
	public int hashCode() {
		int hashCode = 0x811C9DC5;
		hashCode = 0x01000193 * (hashCode ^ image.hashCode());
		hashCode = 0x01000193 * (hashCode ^ Integer.hashCode(durationMillis));
		return hashCode;
	}
}
