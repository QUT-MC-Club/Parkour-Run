package io.github.haykam821.parkourrun.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.parkourrun.game.map.ParkourRunMapConfig;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class ParkourRunConfig {
	public static final Codec<ParkourRunConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			ParkourRunMapConfig.CODEC.fieldOf("map").forGetter(ParkourRunConfig::getMapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(ParkourRunConfig::getPlayerConfig)
		).apply(instance, ParkourRunConfig::new);
	});

	private final ParkourRunMapConfig mapConfig;
	private final PlayerConfig playerConfig;

	public ParkourRunConfig(ParkourRunMapConfig mapConfig, PlayerConfig playerConfig) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
	}

	public ParkourRunMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}
}