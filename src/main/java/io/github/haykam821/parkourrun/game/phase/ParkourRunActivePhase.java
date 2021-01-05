package io.github.haykam821.parkourrun.game.phase;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import io.github.haykam821.parkourrun.Main;
import io.github.haykam821.parkourrun.game.ParkourRunSpawnLogic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.Game;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public class ParkourRunActivePhase {
	private final GameWorld gameWorld;
	private final ParkourRunSpawnLogic spawnLogic;
	private final Set<ServerPlayerEntity> players;
	private final long startTime;

	public ParkourRunActivePhase(GameWorld gameWorld, ParkourRunSpawnLogic spawnLogic) {
		this.gameWorld = gameWorld;
		this.spawnLogic = spawnLogic;
		this.players = Sets.newHashSet(gameWorld.getPlayers());
		this.startTime = gameWorld.getWorld().getTime();
	}

	public static void setRules(Game game) {
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.PVP, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
	}

	public static void open(GameWorld gameWorld, ParkourRunSpawnLogic spawnLogic) {
		ParkourRunActivePhase phase = new ParkourRunActivePhase(gameWorld, spawnLogic);

		gameWorld.openGame(game -> {
			ParkourRunActivePhase.setRules(game);

			// Listeners
			game.on(GameTickListener.EVENT, phase::tick);
			game.on(PlayerAddListener.EVENT, phase::addPlayer);
			game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
		});
	}

	public void tick() {
		Iterator<ServerPlayerEntity> iterator = this.players.iterator();
		while (iterator.hasNext()) {
			ServerPlayerEntity player = iterator.next();

			BlockState state = player.getLandingBlockState();
			if (state.isIn(Main.ENDING_PLATFORMS)) {
				ParkourRunResult result = new ParkourRunResult(player, this.gameWorld.getWorld().getTime() - this.startTime);
				this.gameWorld.getPlayers().forEach(result::announce);

				this.gameWorld.close();
				return;
			}
		}
	}

	private void setSpectator(PlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	public void addPlayer(ServerPlayerEntity player) {
		if (!this.players.contains(player)) {
			this.setSpectator(player);
			this.spawnLogic.spawnPlayer(player);
		}
	}

	public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player at the start
		this.spawnLogic.spawnPlayer(player);
		return ActionResult.FAIL;
	}
}
