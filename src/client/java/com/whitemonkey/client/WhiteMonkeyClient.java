package com.whitemonkey.client;

import com.whitemonkey.client.model.MonkeyModel;
import com.whitemonkey.client.model.MonkeyModelData;
import com.whitemonkey.client.render.MonkeyRenderer;
import com.whitemonkey.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class WhiteMonkeyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(MonkeyModel.LAYER, MonkeyModelData::createBodyLayer);
		EntityRenderers.register(ModEntities.WHITE_MONKEY, MonkeyRenderer::new);
	}
}
