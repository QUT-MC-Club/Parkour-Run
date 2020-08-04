package io.github.haykam821.parkourrun;

import io.github.haykam821.parkourrun.game.ParkourRunConfig;
import io.github.haykam821.parkourrun.game.ParkourRunGame;
import io.github.haykam821.parkourrun.game.map.ParkourRunMapProvider;
import net.fabricmc.api.ModInitializer;
import net.gegy1000.plasmid.game.GameType;
import net.gegy1000.plasmid.game.config.GameMapConfig;
import net.gegy1000.plasmid.game.map.provider.MapProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class Main implements ModInitializer {
	public static final String MOD_ID = "parkourrun";

	private static final Identifier PARKOUR_RUN_ID = new Identifier(MOD_ID, "parkour_run");
	public static final GameType<ParkourRunConfig> PARKOUR_RUN_TYPE = GameType.register(PARKOUR_RUN_ID, (server, config) -> {
		GameMapConfig<ParkourRunConfig> mapConfig = config.getMapConfig();

		RegistryKey<World> dimension = mapConfig.getDimension();
		BlockPos origin = mapConfig.getOrigin();
		ServerWorld world = server.getWorld(dimension);

		return mapConfig.getProvider().createAt(world, origin, config).thenApply(map -> {
			return ParkourRunGame.open(map, config);
		});
	}, ParkourRunConfig.CODEC);

	@Override
	public void onInitialize() {
		MapProvider.REGISTRY.register(PARKOUR_RUN_ID, ParkourRunMapProvider.CODEC);
	}
}