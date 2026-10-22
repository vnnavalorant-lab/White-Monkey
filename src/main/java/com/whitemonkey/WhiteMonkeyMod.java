package com.whitemonkey;

import com.whitemonkey.entity.MonkeyEntity;
import com.whitemonkey.registry.ModEntities;
import com.whitemonkey.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public class WhiteMonkeyMod implements ModInitializer {
	public static final String MOD_ID = "white_monkey";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModEntities.init();
		ModItems.init();
		FabricDefaultAttributeRegistry.register(ModEntities.WHITE_MONKEY, MonkeyEntity.createAttributes());

		// Shift + botão direito em um BLOCO (mesmo com a mão vazia) = coloca o macaco no chão.
		// (O Minecraft não dispara "usar item" com a mão vazia no ar, por isso o bloco é o alvo.)
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
				MonkeyEntity monkey = MonkeyEntity.getShoulderMonkey(player);
				if (monkey != null) {
					if (!level.isClientSide()) {
						BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
						monkey.dismountAt(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					}
					return InteractionResult.SUCCESS;
				}
			}
			return InteractionResult.PASS;
		});

		// Shift + botão direito no ar segurando qualquer item = o macaco desce do ombro.
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
				MonkeyEntity monkey = MonkeyEntity.getShoulderMonkey(player);
				if (monkey != null) {
					if (!level.isClientSide()) {
						monkey.dismountFromShoulder();
					}
					return InteractionResult.SUCCESS;
				}
			}
			return InteractionResult.PASS;
		});

		// Ao sair do jogo, o macaco desce do ombro para não sumir junto com o jogador.
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			MonkeyEntity monkey = MonkeyEntity.getShoulderMonkey(handler.getPlayer());
			if (monkey != null) {
				monkey.dismountFromShoulder();
			}
		});
	}
}
