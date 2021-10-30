package io.github.haykam821.parkourrun.game.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.github.haykam821.parkourrun.Main;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class ParkourRunChunkGenerator extends GameChunkGenerator {
	private static final Identifier STARTS_ID = new Identifier(Main.MOD_ID, "starts");
	private static final Identifier AREAS_ID = new Identifier(Main.MOD_ID, "areas");
	private static final Identifier CONNECTORS_ID = new Identifier(Main.MOD_ID, "connectors");
	private static final Identifier ENDINGS_ID = new Identifier(Main.MOD_ID, "endings");

	private final ParkourRunMap map;
	private final StructureManager structureManager;
	private final Long2ObjectMap<List<PoolStructurePiece>> piecesByChunk = new Long2ObjectOpenHashMap<>();

	private final StructurePool starts;
	private final StructurePool areas;
	private final StructurePool connectors;
	private final StructurePool endings;

	public ParkourRunChunkGenerator(MinecraftServer server, ParkourRunMap map) {
		super(server);
		this.map = map;
		this.structureManager = server.getStructureManager();

		Registry<StructurePool> poolRegistry = server.getRegistryManager().get(Registry.TEMPLATE_POOL_WORLDGEN);
		this.starts = poolRegistry.get(STARTS_ID);
		this.areas = poolRegistry.get(AREAS_ID);
		this.connectors = poolRegistry.get(CONNECTORS_ID);
		this.endings = poolRegistry.get(ENDINGS_ID);

		for (PoolStructurePiece piece : this.generatePieces()) {
			BlockBox box = piece.getBoundingBox();
			int minChunkX = box.minX >> 4;
			int minChunkZ = box.minZ >> 4;
			int maxChunkX = box.maxX >> 4;
			int maxChunkZ = box.maxZ >> 4;

			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
					long chunkPos = ChunkPos.toLong(chunkX, chunkZ);
					List<PoolStructurePiece> piecesByChunk = this.piecesByChunk.computeIfAbsent(chunkPos, p -> new ArrayList<>());
					piecesByChunk.add(piece);
				}
			}
		}
	}

	private void generatePiece(String marker, Set<PoolStructurePiece> pieces, StructurePoolElement element, Random random, BlockPos.Mutable pos) {
		if (!(element instanceof SinglePoolElement)) return;
		Structure structure = ((SinglePoolElement) element).method_27233(this.structureManager);

		BlockBox box = new BlockBox(pos, pos.add(structure.getSize()));
		PoolStructurePiece piece = new PoolStructurePiece(structureManager, element, pos.toImmutable(), 0, BlockRotation.NONE, box);
		pieces.add(piece);

		this.map.getTemplate().getMetadata().addRegion(marker, new BlockBounds(pos, pos.add(structure.getSize())));
		pos.move(Direction.EAST, structure.getSize().getX());
	}

	private Set<PoolStructurePiece> generatePieces() {
		Set<PoolStructurePiece> pieces = new HashSet<>();

		BlockPos.Mutable pos = this.map.getOrigin().mutableCopy();
		Random random = new Random();
		int areaCount = this.map.getConfig().getAreaCount();

		this.generatePiece("start", pieces, this.starts.getRandomElement(random), random, pos);
		for (int index = 0; index < areaCount; index++) {
			this.generatePiece("area", pieces, this.areas.getRandomElement(random), random, pos);
			if (index + 1 < areaCount) {
				this.generatePiece("connector", pieces, this.connectors.getRandomElement(random), random, pos);
			}
		}
		this.generatePiece("ending", pieces, this.endings.getRandomElement(random), random, pos);

		return pieces;
	}

	@Override
	public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
		if (this.piecesByChunk.isEmpty()) {
			return;
		}

		ChunkPos chunkPos = new ChunkPos(region.getCenterChunkX(), region.getCenterChunkZ());
		List<PoolStructurePiece> pieces = this.piecesByChunk.remove(chunkPos.toLong());

		if (pieces != null) {
			BlockBox chunkBox = new BlockBox(chunkPos.getStartX(), 0, chunkPos.getStartZ(), chunkPos.getEndX(), 255, chunkPos.getEndZ());
			for (PoolStructurePiece piece : pieces) {
				piece.generate(region, structures, this, region.getRandom(), chunkBox, this.map.getOrigin(), false);
			}
		}
	}

	@Override
	public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
		return;
	}
}