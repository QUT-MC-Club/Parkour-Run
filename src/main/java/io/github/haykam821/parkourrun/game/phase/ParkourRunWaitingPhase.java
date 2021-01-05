package io.github.haykam821.parkourrun.game.phase;

import java.util.concurrent.CompletableFuture;

import io.github.haykam821.parkourrun.game.ParkourRunConfig;
import io.github.haykam821.parkourrun.game.ParkourRunSpawnLogic;
import io.github.haykam821.parkourrun.game.map.ParkourRunMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorldConfig;

public class ParkourRunWaitingPhase {
	private final GameWorld gameWorld;
	private final ParkourRunConfig config;
	private final ParkourRunSpawnLogic spawnLogic;

	public ParkourRunWaitingPhase(GameWorld gameWorld, ParkourRunMap map, ParkourRunConfig config) {
		this.gameWorld = gameWorld;
		this.config = config;
		this.spawnLogic = new ParkourRunSpawnLogic(map, this.gameWorld.getWorld());
	}

	public static CompletableFuture<Void> open(GameOpenContext<ParkourRunConfig> context) {
		return CompletableFuture.supplyAsync(() -> {
			return new ParkourRunMap(context.getConfig().getMapConfig());
		}, Util.getMainWorkerExecutor()).thenAccept(map -> {
			BubbleWorldConfig worldConfig = new BubbleWorldConfig()
				.setGenerator(map.createGenerator(context.getServer()))
				.setDefaultGameMode(GameMode.ADVENTURE);

			GameWorld gameWorld = context.openWorld(worldConfig);
			ParkourRunWaitingPhase phase = new ParkourRunWaitingPhase(gameWorld, map, context.getConfig());
			gameWorld.openGame(game -> {
				ParkourRunActivePhase.setRules(game);

				// Listeners
				game.on(PlayerAddListener.EVENT, phase::addPlayer);
				game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
				game.on(OfferPlayerListener.EVENT, phase::offerPlayer);
				game.on(RequestStartListener.EVENT, phase::requestStart);
			});
		});
	}

	private boolean isFull() {
		return this.gameWorld.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	public JoinResult offerPlayer(ServerPlayerEntity player) {
		return this.isFull() ? JoinResult.gameFull() : JoinResult.ok();
	}

	public StartResult requestStart() {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (this.gameWorld.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.notEnoughPlayers();
		}

		ParkourRunActivePhase.open(this.gameWorld, this.spawnLogic);
		return StartResult.ok();
	}

	public void addPlayer(ServerPlayerEntity player) {
		this.spawnPlayer(player);
	}

	public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player at the start
		this.spawnPlayer(player);
		return ActionResult.FAIL;
	}

	public void spawnPlayer(ServerPlayerEntity player) {
		this.spawnLogic.spawnPlayer(player);
	}
}
