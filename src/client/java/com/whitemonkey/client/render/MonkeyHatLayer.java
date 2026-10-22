package com.whitemonkey.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.whitemonkey.client.model.MonkeyHatModel;
import com.whitemonkey.client.model.MonkeyModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;

/**
 * Capacete na cabeça do macaco, desenhado do mesmo jeito que o jogo desenha o capacete de um jogador
 * (EquipmentLayerRenderer: textura de armadura, cor de couro, trims, brilho de encantamento).
 * Itens de cabeça sem modelo de armadura (abóbora, cabeças de mob...) usam o display "head" do item.
 */
public class MonkeyHatLayer extends RenderLayer<MonkeyRenderState, MonkeyModel> {
	private final MonkeyHatModel hatModel;
	private final EquipmentLayerRenderer equipmentRenderer;

	public MonkeyHatLayer(MonkeyRenderer renderer, MonkeyHatModel hatModel, EquipmentLayerRenderer equipmentRenderer) {
		super(renderer);
		this.hatModel = hatModel;
		this.equipmentRenderer = equipmentRenderer;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
					   MonkeyRenderState state, float yRot, float xRot) {
		if (state.hatStack.isEmpty()) return;

		MonkeyModel model = this.getParentModel();
		model.setupAnim(state);

		poseStack.pushPose();
		model.translateTo("head", poseStack); // mesma origem (pescoço) do modelo humanoide

		var equippable = state.hatStack.get(DataComponents.EQUIPPABLE);
		var assetKey = equippable == null ? null : equippable.assetId().orElse(null);
		if (assetKey != null) {
			this.equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.HUMANOID, assetKey,
					this.hatModel, state, state.hatStack, poseStack, collector, light, state.outlineColor);
		} else if (!state.hatItem.isEmpty()) {
			poseStack.translate(0.0F, -0.25F, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			poseStack.scale(0.625F, -0.625F, -0.625F);
			state.hatItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
		}
		poseStack.popPose();
	}
}
