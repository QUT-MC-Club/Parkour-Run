package io.github.haykam821.parkourrun.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.config.GameMapConfig;
import net.gegy1000.plasmid.game.config.PlayerConfig;

public class ParkourRunConfig implements GameConfig {
	public static final Codec<ParkourRunConfig> CODEC = RecordCodecBuilder.create(instance -> {
		Codec<GameMapConfig<ParkourRunConfig>> mapCodec = GameMapConfig.codec();
		return instance.group(
			mapCodec.fieldOf("map").forGetter(ParkourRunConfig::getMapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(ParkourRunConfig::getPlayerConfig)
		).apply(instance, ParkourRunConfig::new);
	});

	private final GameMapConfig<ParkourRunConfig> mapConfig;
	private final PlayerConfig playerConfig;

	public ParkourRunConfig(GameMapConfig<ParkourRunConfig> mapConfig, PlayerConfig playerConfig) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
	}

	public GameMapConfig<ParkourRunConfig> getMapConfig() {
		return this.mapConfig;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}
}