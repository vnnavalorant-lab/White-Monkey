package com.whitemonkey.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.whitemonkey.client.model.MonkeyModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Desenha o item que o macaco está comendo exatamente no bone "item_eat" do modelo. */
public class MonkeyEatItemLayer extends RenderLayer<MonkeyRenderState, MonkeyModel> {
	/** Ajuste fino do tamanho do item (1.0 = tamanho "GROUND" padrão). */
	private static final float ITEM_SCALE = 0.7F;

	public MonkeyEatItemLayer(MonkeyRenderer renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
					   MonkeyRenderState state, float yRot, float xRot) {
		if (!state.eating || state.eatItem.isEmpty()) return;

		MonkeyModel model = this.getParentModel();
		model.setupAnim(state); // garante os ossos na pose do frame atual
		if (model.itemEatScale() < 0.02F) return; // animação "eating" esconde o item (scale 0)

		poseStack.pushPose();
		model.translateToItemEat(poseStack);
		// desfaz o flip (-1,-1,1) do espaço de modelo e vira o item para frente
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.rotate(Axis.YP.rotationDegrees(180.0F));
		poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
		state.eatItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
	}
}
