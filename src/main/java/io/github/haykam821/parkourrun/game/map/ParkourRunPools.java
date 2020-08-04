package io.github.haykam821.parkourrun.game.map;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.haykam821.parkourrun.Main;
import net.minecraft.structure.pool.LegacySinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.Identifier;

public class ParkourRunPools {
	private static final Identifier STARTS_ID = new Identifier(Main.MOD_ID, "starts");
	public static final StructurePool STARTS = ParkourRunPools.createPool(STARTS_ID, elements -> {
		elements.add(new LegacySinglePoolElement("parkourrun:starts/start"));
	});

	private static final Identifier AREAS_ID = new Identifier(Main.MOD_ID, "areas");
	public static final StructurePool AREAS = ParkourRunPools.createPool(AREAS_ID, elements -> {
		elements.add(new LegacySinglePoolElement("parkourrun:areas/around_the_gate"));
		elements.add(new LegacySinglePoolElement("parkourrun:areas/ruined_portal"));
	});

	private static final Identifier CONNECTORS_ID = new Identifier(Main.MOD_ID, "connectors");
	public static final StructurePool CONNECTORS = ParkourRunPools.createPool(CONNECTORS_ID, elements -> {
		elements.add(new LegacySinglePoolElement("parkourrun:connectors/staircase_connector"));
	});

	private static final Identifier ENDINGS_ID = new Identifier(Main.MOD_ID, "endings");
	public static final StructurePool ENDINGS = ParkourRunPools.createPool(ENDINGS_ID, elements -> {
		elements.add(new LegacySinglePoolElement("parkourrun:endings/ending"));
	});

	private static StructurePool createPool(Identifier id, Consumer<List<StructurePoolElement>> elementSupplier) {
		List<StructurePoolElement> elements = Lists.newArrayList();
		elementSupplier.accept(elements);

		List<Pair<StructurePoolElement, Integer>> elementCounts = elements.stream().collect(Collectors.mapping(element -> {
			return Pair.of(element, 1);
		}, Collectors.toList()));
		return new StructurePool(id, new Identifier("empty"), elementCounts, StructurePool.Projection.RIGID);
	}
}