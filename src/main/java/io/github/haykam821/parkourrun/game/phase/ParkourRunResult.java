package io.github.haykam821.parkourrun.game.phase;

import java.util.Locale;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ParkourRunResult {
	private final PlayerEntity winner;
	private final long time;

	public ParkourRunResult(PlayerEntity winner, long time) {
		this.winner = winner;
		this.time = time;
	}

	public PlayerEntity getWinner() {
		return this.winner;
	}

	public float getTimeInSeconds() {
		return this.time / 20f;
	}

	public Text getText() {
		return this.winner.getDisplayName().shallowCopy().append(String.format(" has won Parkour Run in %,d seconds!", (long) this.getTimeInSeconds(), Locale.ROOT)).formatted(Formatting.GOLD);
	}

	public void announce(PlayerEntity to) {
		to.sendMessage(this.getText(), false);
	}
}