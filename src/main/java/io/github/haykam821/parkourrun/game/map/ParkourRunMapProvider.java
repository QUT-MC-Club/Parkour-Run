package io.github.haykam821.parkourrun.game.map;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.parkourrun.game.ParkourRunConfig;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.map.GameMapBuilder;
import net.gegy1000.plasmid.game.map.provider.MapProvider;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ParkourRunMapProvider implements MapProvider<ParkourRunConfig> {
	public static final Codec<ParkourRunMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.fieldOf("areaCount").forGetter(map -> map.areaCount)
		).apply(instance, ParkourRunMapProvider::new);
	});

	public final int areaCount;

	public ParkourRunMapProvider(int areaCount) {
		this.areaCount = areaCount;
	}
	
	@Override
	public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin, ParkourRunConfig config) {
		BlockBounds bounds = new BlockBounds(new BlockPos(0, 0, 0));
		GameMapBuilder builder = GameMapBuilder.open(world, origin, bounds);
		return CompletableFuture.supplyAsync(() -> {
			this.build(builder, config);
			return builder.build();
		}, world.getServer());
	}

	public void build(GameMapBuilder builder, ParkourRunConfig config) {
		BlockPos.Mutable pos = builder.getOrigin().mutableCopy();
		Random random = builder.getWorld().getRandom();

		this.place(builder.getWorld(), ParkourRunPools.STARTS.getRandomElement(random), random, pos);
		for (int index = 0; index < this.areaCount; index++) {
			this.place(builder.getWorld(), ParkourRunPools.AREAS.getRandomElement(random), random, pos);
			if (index + 1 < this.areaCount) {
				this.place(builder.getWorld(), ParkourRunPools.CONNECTORS.getRandomElement(random), random, pos);
			}
		}
		this.place(builder.getWorld(), ParkourRunPools.ENDINGS.getRandomElement(random), random, pos);
		
 		builder.addRegion("spawn", new BlockBounds(new BlockPos(4, 1, 5)));
	}

	private void place(ServerWorld world, StructurePoolElement element, Random random, BlockPos.Mutable pos) {
		if (!(element instanceof SinglePoolElement)) return;
		Structure structure = ((SinglePoolElement) element).method_27233(world.getStructureManager());

		StructurePlacementData placementData = new StructurePlacementData();

		boolean mirror = random.nextBoolean();
		if (mirror) {
			placementData.setMirror(BlockMirror.LEFT_RIGHT);
		}

		structure.place(world, mirror ? pos.offset(Direction.SOUTH, structure.getSize().getZ() - 1) : pos, placementData, random);
		pos.move(Direction.EAST, structure.getSize().getX());
	}

	@Override
	public Codec<ParkourRunMapProvider> getCodec() {
		return CODEC;
	}
}