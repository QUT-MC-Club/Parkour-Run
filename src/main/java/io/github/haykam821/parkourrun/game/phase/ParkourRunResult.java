package io.github.haykam821.parkourrun.game.phase;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ParkourRunResult {
	private final PlayerEntity winner;

	public ParkourRunResult(PlayerEntity winner) {
		this.winner = winner;
	}

	public PlayerEntity getWinner() {
		return this.winner;
	}

	public Text getText() {
		return this.winner.getDisplayName().shallowCopy().append(" has won Parkour Run!").formatted(Formatting.GOLD);
	}

	public void announce(PlayerEntity to) {
		to.sendMessage(this.getText(), false);
	}
}