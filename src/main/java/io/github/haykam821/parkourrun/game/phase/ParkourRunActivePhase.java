package io.github.haykam821.parkourrun.game.phase;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import io.github.haykam821.parkourrun.Main;
import io.github.haykam821.parkourrun.game.ParkourRunSpawnLogic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ParkourRunActivePhase {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final ParkourRunSpawnLogic spawnLogic;
	private final Set<ServerPlayerEntity> players;
	private final long startTime;

	public ParkourRunActivePhase(GameSpace gameSpace, ServerWorld world, ParkourRunSpawnLogic spawnLogic) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.spawnLogic = spawnLogic;
		this.players = Sets.newHashSet(gameSpace.getPlayers());
		this.startTime = world.getTime();
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.PVP);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
	}

	public static void open(GameSpace gameSpace, ServerWorld world, ParkourRunSpawnLogic spawnLogic) {
		ParkourRunActivePhase phase = new ParkourRunActivePhase(gameSpace, world, spawnLogic);

		gameSpace.setActivity(activity -> {
			ParkourRunActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(GamePlayerEvents.OFFER, phase::offerPlayer);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
		});
	}

	public void tick() {
		Iterator<ServerPlayerEntity> iterator = this.players.iterator();
		while (iterator.hasNext()) {
			ServerPlayerEntity player = iterator.next();

			BlockState state = player.getLandingBlockState();
			if (state.isIn(Main.ENDING_PLATFORMS)) {
				ParkourRunResult result = new ParkourRunResult(player, this.world.getTime() - this.startTime);
				this.gameSpace.getPlayers().forEach(result::announce);

				this.gameSpace.close(GameCloseReason.FINISHED);
				return;
			}
		}
	}

	public PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.spawnLogic.getSpawnPos()).and(() -> {
			if (this.players.contains(offer.player())) {
				offer.player().changeGameMode(GameMode.ADVENTURE);
			} else {
				offer.player().changeGameMode(GameMode.SPECTATOR);
			}
		});
	}

	public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player at the start
		this.spawnLogic.spawnPlayer(player);
		return ActionResult.FAIL;
	}
}
