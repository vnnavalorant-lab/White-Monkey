package com.whitemonkey.registry;

import com.whitemonkey.WhiteMonkeyMod;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
	private ModItems() {}

	public static final Item WHITE_MONKEY_SPAWN_EGG = register(
			"white_monkey_spawn_egg",
			SpawnEggItem::new,
			new Item.Properties().spawnEgg(ModEntities.WHITE_MONKEY));

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WhiteMonkeyMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
				.register(output -> output.accept(WHITE_MONKEY_SPAWN_EGG));
	}
}
