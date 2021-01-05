package io.github.haykam821.parkourrun.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ParkourRunMapConfig {
	public static final Codec<ParkourRunMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.fieldOf("areaCount").forGetter(ParkourRunMapConfig::getAreaCount)
		).apply(instance, ParkourRunMapConfig::new);
	});

	private final int areaCount;

	public ParkourRunMapConfig(int areaCount) {
		this.areaCount = areaCount;
	}
	
	public int getAreaCount() {
		return this.areaCount;
	}
}
