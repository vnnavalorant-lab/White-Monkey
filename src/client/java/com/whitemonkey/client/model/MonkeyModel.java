package com.whitemonkey.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.whitemonkey.WhiteMonkeyMod;
import com.whitemonkey.client.render.MonkeyRenderState;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class MonkeyModel extends EntityModel<MonkeyRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(WhiteMonkeyMod.id("white_monkey"), "main");

	private final Map<String, ModelPart> bones = new HashMap<>();
	private final ModelPart head;
	private final ModelPart[] itemEatChain;

	public MonkeyModel(ModelPart root) {
		super(root);
		ModelPart[] chain = new ModelPart[0];
		for (String[] path : MonkeyModelData.BONE_PATHS) {
			ModelPart part = root;
			ModelPart[] acc = new ModelPart[path.length];
			for (int i = 0; i < path.length; i++) {
				part = part.getChild(path[i]);
				acc[i] = part;
			}
			String name = path[path.length - 1];
			this.bones.put(name, part);
			if (name.equals("item_eat")) chain = acc;
		}
		this.head = this.bones.get("head");
		this.itemEatChain = chain;
	}

	@Override
	public void setupAnim(MonkeyRenderState state) {
		super.setupAnim(state); // reseta a pose
		if (state.clip != null) {
			state.clip.apply(this.bones, state.clipSeconds);
		}
		if (this.head != null) {
			// Cabeça acompanha o olhar (um pouco amortecido).
			this.head.yRot += Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD * 0.8F;
			if (!state.eating) {
				this.head.xRot += Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.6F;
			}
		}
	}

	/** Escala atual do bone item_eat (0 = item escondido). */
	public float itemEatScale() {
		ModelPart part = this.itemEatChain.length > 0 ? this.itemEatChain[this.itemEatChain.length - 1] : null;
		return part == null ? 0.0F : Math.min(part.xScale, Math.min(part.yScale, part.zScale));
	}

	/** Aplica no PoseStack a transformação completa do bone item_eat (root > body > item_eat). */
	public void translateToItemEat(PoseStack poseStack) {
		for (ModelPart part : this.itemEatChain) {
			part.translateAndRotate(poseStack);
		}
	}
}
