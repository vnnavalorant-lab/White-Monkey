package com.whitemonkey.client.render;

import com.whitemonkey.client.anim.MonkeyClip;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class MonkeyRenderState extends LivingEntityRenderState {
	public MonkeyClip clip;
	public float clipSeconds;
	public boolean eating;
	public boolean onShoulder;
	/** Item que aparece no bone "item_eat". */
	public final ItemStackRenderState eatItem = new ItemStackRenderState();
	/** Capacete na cabeça. */
	public ItemStack hatStack = ItemStack.EMPTY;
	public final ItemStackRenderState hatItem = new ItemStackRenderState();
}
