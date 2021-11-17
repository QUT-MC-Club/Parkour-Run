package io.github.haykam821.parkourrun;

import io.github.haykam821.parkourrun.game.ParkourRunConfig;
import io.github.haykam821.parkourrun.game.phase.ParkourRunWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;

public class Main implements ModInitializer {
	public static final String MOD_ID = "parkourrun";

	private static final Identifier ENDING_PLATFORMS_ID = new Identifier(MOD_ID, "ending_platforms");
	public static final Tag<Block> ENDING_PLATFORMS = TagFactory.BLOCK.create(ENDING_PLATFORMS_ID);

	private static final Identifier PARKOUR_RUN_ID = new Identifier(MOD_ID, "parkour_run");
	public static final GameType<ParkourRunConfig> PARKOUR_RUN_TYPE = GameType.register(PARKOUR_RUN_ID, ParkourRunConfig.CODEC, ParkourRunWaitingPhase::open);

	@Override
	public void onInitialize() {
		return;
	}
}