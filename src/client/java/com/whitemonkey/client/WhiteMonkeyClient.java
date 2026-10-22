package com.whitemonkey.client;

import com.whitemonkey.client.model.MonkeyHatModel;
import com.whitemonkey.client.model.MonkeyModel;
import com.whitemonkey.client.model.MonkeyModelData;
import com.whitemonkey.client.render.MonkeyRenderer;
import com.whitemonkey.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class WhiteMonkeyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(MonkeyModel.LAYER, MonkeyModelData::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(MonkeyHatModel.LAYER, MonkeyHatModel::createLayer);
		EntityRenderers.register(ModEntities.WHITE_MONKEY, MonkeyRenderer::new);
	}
}
