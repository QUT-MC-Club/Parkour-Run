package io.github.haykam821.parkourrun.game;

import io.github.haykam821.parkourrun.game.map.ParkourRunMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class ParkourRunSpawnLogic {
	private final ParkourRunMap map;
	private final ServerWorld world;

	public ParkourRunSpawnLogic(ParkourRunMap map, ServerWorld world) {
		this.map = map;
		this.world = world;
	}

	public void spawnPlayer(ServerPlayerEntity player) {
		BlockBounds spawn = this.map.getSpawn();
		if (spawn != null) {
			BlockPos pos = new BlockPos(spawn.getCenter());
			player.teleport(this.world, pos.getX(), pos.getY(), pos.getZ(), player.yaw, player.pitch);
		}
	}
}