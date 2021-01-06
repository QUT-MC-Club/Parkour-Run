package io.github.haykam821.parkourrun.game.phase;

import io.github.haykam821.parkourrun.game.ParkourRunConfig;
import io.github.haykam821.parkourrun.game.ParkourRunSpawnLogic;
import io.github.haykam821.parkourrun.game.map.ParkourRunMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;

public class ParkourRunWaitingPhase {
	private final GameSpace gameSpace;
	private final ParkourRunConfig config;
	private final ParkourRunSpawnLogic spawnLogic;

	public ParkourRunWaitingPhase(GameSpace gameSpace, ParkourRunMap map, ParkourRunConfig config) {
		this.gameSpace = gameSpace;
		this.config = config;
		this.spawnLogic = new ParkourRunSpawnLogic(map, this.gameSpace.getWorld());
	}

	public static GameOpenProcedure open(GameOpenContext<ParkourRunConfig> context) {
		ParkourRunMap map = new ParkourRunMap(context.getConfig().getMapConfig());
		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
			.setGenerator(map.createGenerator(context.getServer()))
			.setDefaultGameMode(GameMode.ADVENTURE);

		return context.createOpenProcedure(worldConfig, game -> {
			ParkourRunWaitingPhase phase = new ParkourRunWaitingPhase(game.getSpace(), map, context.getConfig());
			GameWaitingLobby.applyTo(game, context.getConfig().getPlayerConfig());

			ParkourRunActivePhase.setRules(game);

			// Listeners
			game.on(PlayerAddListener.EVENT, phase::addPlayer);
			game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
			game.on(OfferPlayerListener.EVENT, phase::offerPlayer);
			game.on(RequestStartListener.EVENT, phase::requestStart);
		});
	}

	private boolean isFull() {
		return this.gameSpace.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	public JoinResult offerPlayer(ServerPlayerEntity player) {
		return this.isFull() ? JoinResult.gameFull() : JoinResult.ok();
	}

	public StartResult requestStart() {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (this.gameSpace.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.NOT_ENOUGH_PLAYERS;
		}

		ParkourRunActivePhase.open(this.gameSpace, this.spawnLogic);
		return StartResult.OK;
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
