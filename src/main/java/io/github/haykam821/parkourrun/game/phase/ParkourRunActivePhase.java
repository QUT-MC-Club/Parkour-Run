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
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;

public class ParkourRunActivePhase {
	private final GameSpace gameSpace;
	private final ParkourRunSpawnLogic spawnLogic;
	private final Set<ServerPlayerEntity> players;
	private final long startTime;

	public ParkourRunActivePhase(GameSpace gameSpace, ParkourRunSpawnLogic spawnLogic) {
		this.gameSpace = gameSpace;
		this.spawnLogic = spawnLogic;
		this.players = Sets.newHashSet(gameSpace.getPlayers());
		this.startTime = gameSpace.getWorld().getTime();
	}

	public static void setRules(GameLogic game) {
		game.deny(GameRule.CRAFTING);
		game.deny(GameRule.PORTALS);
		game.deny(GameRule.PVP);
		game.deny(GameRule.FALL_DAMAGE);
		game.deny(GameRule.HUNGER);
	}

	public static void open(GameSpace gameSpace, ParkourRunSpawnLogic spawnLogic) {
		ParkourRunActivePhase phase = new ParkourRunActivePhase(gameSpace, spawnLogic);

		gameSpace.openGame(game -> {
			ParkourRunActivePhase.setRules(game);

			// Listeners
			game.listen(GameTickListener.EVENT, phase::tick);
			game.listen(PlayerAddListener.EVENT, phase::addPlayer);
			game.listen(PlayerDeathListener.EVENT, phase::onPlayerDeath);
		});
	}

	public void tick() {
		Iterator<ServerPlayerEntity> iterator = this.players.iterator();
		while (iterator.hasNext()) {
			ServerPlayerEntity player = iterator.next();

			BlockState state = player.getLandingBlockState();
			if (state.isIn(Main.ENDING_PLATFORMS)) {
				ParkourRunResult result = new ParkourRunResult(player, this.gameSpace.getWorld().getTime() - this.startTime);
				this.gameSpace.getPlayers().forEach(result::announce);

				this.gameSpace.close(GameCloseReason.FINISHED);
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
