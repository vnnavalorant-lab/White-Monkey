package com.whitemonkey.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.whitemonkey.WhiteMonkeyMod;
import com.whitemonkey.client.anim.MonkeyAnimations;
import com.whitemonkey.client.model.MonkeyModel;
import com.whitemonkey.entity.MonkeyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MonkeyRenderer extends MobRenderer<MonkeyEntity, MonkeyRenderState, MonkeyModel> {
	private static final Identifier TEXTURE = WhiteMonkeyMod.id("textures/entity/white_monkey.png");

	// posição/escala no ombro direito do jogador (em blocos, referencial do jogador)
	private static final float SHOULDER_X = 0.30F;
	private static final float SHOULDER_Y = -0.38F;
	private static final float SHOULDER_Z = 0.0F;
	private static final float SHOULDER_SCALE = 0.55F;

	private final ItemModelResolver itemResolver;

	public MonkeyRenderer(EntityRendererProvider.Context context) {
		super(context, new MonkeyModel(context.bakeLayer(MonkeyModel.LAYER)), 0.35F);
		this.itemResolver = context.getItemModelResolver();
		this.addLayer(new MonkeyEatItemLayer(this));
	}

	@Override
	public MonkeyRenderState createRenderState() {
		return new MonkeyRenderState();
	}

	@Override
	public Identifier getTextureLocation(MonkeyRenderState state) {
		return TEXTURE;
	}

	@Override
	public void extractRenderState(MonkeyEntity entity, MonkeyRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);

		state.onShoulder = entity.isOnShoulder();
		state.eating = entity.isEating();

		if (state.onShoulder && entity.getVehicle() instanceof Player player) {
			// segue a rotação do corpo/cabeça do jogador
			state.bodyRot = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
			float head = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
			state.yRot = Mth.wrapDegrees(head - state.bodyRot);
			state.xRot = Mth.lerp(partialTick, player.xRotO, player.getXRot());
		}

		if (state.eating) {
			state.clip = MonkeyAnimations.EATING;
			state.clipSeconds = (entity.getEatTicks() + partialTick) / 20.0F;
		} else if (!state.onShoulder && state.walkAnimationSpeed > 0.03F) {
			state.clip = MonkeyAnimations.WALK;
			state.clipSeconds = state.walkAnimationPos / 12.0F; // 1 ciclo a cada ~12 unidades de passo
		} else {
			state.clip = MonkeyAnimations.IDLE;
			state.clipSeconds = state.ageInTicks / 20.0F;
		}

		ItemStack eat = entity.getEatStack();
		if (state.eating && !eat.isEmpty()) {
			this.itemResolver.updateForLiving(state.eatItem, eat, ItemDisplayContext.GROUND, entity);
		} else {
			state.eatItem.clear();
		}
	}

	@Override
	protected void setupRotations(MonkeyRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		if (state.onShoulder) {
			poseStack.translate(SHOULDER_X, SHOULDER_Y, SHOULDER_Z);
			poseStack.scale(SHOULDER_SCALE, SHOULDER_SCALE, SHOULDER_SCALE);
		}
	}
}
