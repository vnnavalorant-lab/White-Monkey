package com.whitemonkey.registry;

import com.whitemonkey.WhiteMonkeyMod;
import com.whitemonkey.entity.MonkeyEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	private ModEntities() {}

	public static final ResourceKey<EntityType<?>> WHITE_MONKEY_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, WhiteMonkeyMod.id("white_monkey"));

	public static final EntityType<MonkeyEntity> WHITE_MONKEY = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			WHITE_MONKEY_KEY,
			EntityType.Builder.<MonkeyEntity>of(MonkeyEntity::new, MobCategory.CREATURE)
					.sized(0.6f, 0.95f)
					.clientTrackingRange(10)
					.build(WHITE_MONKEY_KEY));

	public static void init() {
	}
}
