package com.whitemonkey.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.whitemonkey.WhiteMonkeyMod;
import com.whitemonkey.client.anim.MonkeyAnimations;
import com.whitemonkey.client.model.MonkeyHatModel;
import com.whitemonkey.client.model.MonkeyModel;
import com.whitemonkey.entity.MonkeyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MonkeyRenderer extends MobRenderer<MonkeyEntity, MonkeyRenderState, MonkeyModel> {
	private static final Identifier TEXTURE = WhiteMonkeyMod.id("textures/entity/white_monkey.png");

	// escala do macaco quando está no ombro (a posição é calculada em MonkeyEntity#followOwner)
	private static final float SHOULDER_SCALE = 0.55F;

	private final ItemModelResolver itemResolver;

	public MonkeyRenderer(EntityRendererProvider.Context context) {
		super(context, new MonkeyModel(context.bakeLayer(MonkeyModel.LAYER)), 0.35F);
		this.itemResolver = context.getItemModelResolver();
		this.addLayer(new MonkeyEatItemLayer(this));
		this.addLayer(new MonkeyHatLayer(this,
				new MonkeyHatModel(context.bakeLayer(MonkeyHatModel.LAYER)), context.getEquipmentRenderer()));
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

		if (state.onShoulder) {
			// no ombro: SOMENTE idle
			state.eating = false;
			state.clip = MonkeyAnimations.IDLE;
			state.clipSeconds = state.ageInTicks / 20.0F;
		} else if (state.eating) {
			state.clip = MonkeyAnimations.EATING;
			state.clipSeconds = (entity.getEatTicks() + partialTick) / 20.0F;
		} else if (state.walkAnimationSpeed > 0.03F) {
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

		ItemStack hat = entity.getHelmet();
		state.hatStack = hat.copy();
		if (!hat.isEmpty()) {
			this.itemResolver.updateForLiving(state.hatItem, hat, ItemDisplayContext.HEAD, entity);
		} else {
			state.hatItem.clear();
		}
	}

	@Override
	protected void setupRotations(MonkeyRenderState state, PoseStack poseStack, float bodyRot, float scale) {
		super.setupRotations(state, poseStack, bodyRot, scale);
		if (state.onShoulder) {
			poseStack.scale(SHOULDER_SCALE, SHOULDER_SCALE, SHOULDER_SCALE);
		}
	}
}
