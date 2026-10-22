package com.whitemonkey.client.anim;

import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

/** Animação (keyframes do Blockbench já convertidos para o espaço do Minecraft). */
public final class MonkeyClip {
	public static final int POSITION = 0;
	public static final int ROTATION = 1;
	public static final int SCALE = 2;

	public record Track(String bone, int channel, float[] times, float[][] values) {}

	public final String name;
	public final float length;
	public final boolean loop;
	private final Track[] tracks;

	public MonkeyClip(String name, float length, boolean loop, Track[] tracks) {
		this.name = name;
		this.length = length;
		this.loop = loop;
		this.tracks = tracks;
	}

	/** Soma a animação na pose atual dos ossos (chame depois de resetPose). */
	public void apply(Map<String, ModelPart> bones, float seconds) {
		float t = this.loop && this.length > 0 ? seconds % this.length : Math.min(seconds, this.length);
		if (t < 0) t += this.length;
		float[] v = new float[3];
		for (Track track : this.tracks) {
			ModelPart part = bones.get(track.bone());
			if (part == null) continue;
			sample(track, t, v);
			switch (track.channel()) {
				case POSITION -> {
					part.x += v[0];
					part.y += v[1];
					part.z += v[2];
				}
				case ROTATION -> {
					part.xRot += (float) Math.toRadians(v[0]);
					part.yRot += (float) Math.toRadians(v[1]);
					part.zRot += (float) Math.toRadians(v[2]);
				}
				case SCALE -> {
					part.xScale *= v[0];
					part.yScale *= v[1];
					part.zScale *= v[2];
				}
				default -> { }
			}
		}
	}

	private static void sample(Track tr, float time, float[] out) {
		float[] times = tr.times();
		float[][] vals = tr.values();
		int n = times.length;
		if (n == 1 || time <= times[0]) { copy(vals[0], out); return; }
		if (time >= times[n - 1]) { copy(vals[n - 1], out); return; }
		int i = 0;
		while (i < n - 2 && time >= times[i + 1]) i++;
		float span = times[i + 1] - times[i];
		float d = span > 0 ? (time - times[i]) / span : 0;
		float[] p0 = vals[Math.max(i - 1, 0)], p1 = vals[i], p2 = vals[i + 1], p3 = vals[Math.min(i + 2, n - 1)];
		for (int k = 0; k < 3; k++) {
			// Catmull-Rom (mesma interpolação "catmullrom" do Blockbench)
			out[k] = 0.5F * (2 * p1[k] + (-p0[k] + p2[k]) * d
					+ (2 * p0[k] - 5 * p1[k] + 4 * p2[k] - p3[k]) * d * d
					+ (-p0[k] + 3 * p1[k] - 3 * p2[k] + p3[k]) * d * d * d);
		}
	}

	private static void copy(float[] a, float[] out) {
		out[0] = a[0]; out[1] = a[1]; out[2] = a[2];
	}
}
