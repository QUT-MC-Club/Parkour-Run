package io.github.haykam821.parkourrun.game;

import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ParkourRunSpawnLogic {
	private final GameMap map;

	public ParkourRunSpawnLogic(GameMap map) {
		this.map = map;
	}

	public void spawnPlayer(ServerPlayerEntity player) {
		BlockBounds spawn = this.map.getFirstRegion("spawn");
		if (spawn != null) {
			BlockPos pos = new BlockPos(spawn.getCenter());
			player.teleport(this.map.getWorld(), pos.getX(), pos.getY(), pos.getZ(), player.yaw, player.pitch);
		}
	}
}