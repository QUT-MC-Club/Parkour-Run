package io.github.haykam821.parkourrun.game.phase;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import io.github.haykam821.parkourrun.Main;
import io.github.haykam821.parkourrun.game.ParkourRunSpawnLogic;
import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.event.GameTickListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerDeathListener;
import net.gegy1000.plasmid.game.event.PlayerRejoinListener;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class ParkourRunActivePhase {
	private final ParkourRunSpawnLogic spawnLogic;
	private final Set<UUID> players;

	public ParkourRunActivePhase(GameMap map, Set<UUID> players) {
		this.spawnLogic = new ParkourRunSpawnLogic(map);
		this.players = players;
	}

	public static void setRules(Game.Builder builder) {
		builder.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);
		builder.setRule(GameRule.ALLOW_PORTALS, RuleResult.DENY);
		builder.setRule(GameRule.ALLOW_PVP, RuleResult.DENY);
		builder.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		builder.setRule(GameRule.ENABLE_HUNGER, RuleResult.DENY);
	}

	public static Game open(GameMap map, Set<UUID> players) {
		ParkourRunActivePhase game = new ParkourRunActivePhase(map, players);

		Game.Builder builder = Game.builder();
		builder.setMap(map);

		ParkourRunActivePhase.setRules(builder);

		// Listeners
		builder.on(GameTickListener.EVENT, game::tick);
		builder.on(PlayerAddListener.EVENT, game::addPlayer);
		builder.on(PlayerDeathListener.EVENT, game::onPlayerDeath);
		builder.on(PlayerRejoinListener.EVENT, game::rejoinPlayer);

		return builder.build();
	}

	public void tick(Game game) {
		Iterator<ServerPlayerEntity> iterator = game.onlinePlayers().iterator();
		while (iterator.hasNext()) {
			ServerPlayerEntity player = iterator.next();

			BlockState state = player.getLandingBlockState();
			if (state.isIn(Main.ENDING_PLATFORMS)) {
				ParkourRunResult result = new ParkourRunResult(player);
				game.onlinePlayers().forEach(result::announce);

				game.close();
				return;
			}
		}
	}

	private void setSpectator(PlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	public void addPlayer(Game game, ServerPlayerEntity player) {
		if (!this.players.contains(player.getUuid())) {
			this.setSpectator(player);
			this.spawnLogic.spawnPlayer(player);
		}
	}

	public boolean onPlayerDeath(Game game, ServerPlayerEntity player, DamageSource source) {
		// Respawn player at the start
		this.spawnLogic.spawnPlayer(player);
		return true;
	}

	public void rejoinPlayer(Game game, ServerPlayerEntity player) {
		this.setSpectator(player);
		this.spawnLogic.spawnPlayer(player);
	}
}