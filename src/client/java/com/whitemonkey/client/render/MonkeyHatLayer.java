package com.whitemonkey.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.whitemonkey.client.model.MonkeyModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Desenha o capacete na cabeça do macaco (acompanha o osso "head" e suas animações). */
public class MonkeyHatLayer extends RenderLayer<MonkeyRenderState, MonkeyModel> {
	public MonkeyHatLayer(MonkeyRenderer renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
					   MonkeyRenderState state, float yRot, float xRot) {
		if (state.hatItem.isEmpty()) return;

		MonkeyModel model = this.getParentModel();
		model.setupAnim(state);

		poseStack.pushPose();
		model.translateTo("head", poseStack);
		// mesmo ajuste do CustomHeadLayer do jogo: centro da cabeça (8 px) e item virado para frente
		poseStack.translate(0.0F, -0.25F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.scale(0.625F, -0.625F, -0.625F);
		state.hatItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
	}
}
