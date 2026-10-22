package com.whitemonkey.client.model;

import com.whitemonkey.WhiteMonkeyMod;
import com.whitemonkey.client.render.MonkeyRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

/**
 * Modelo de "armadura de cabeça" idêntico ao do capacete de um jogador (cubo 8x8x8 com deformação 1.0,
 * textura 64x32). É desenhado sobre o osso "head" do macaco, que também tem a cabeça 8x8x8.
 */
public class MonkeyHatModel extends EntityModel<MonkeyRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(WhiteMonkeyMod.id("white_monkey"), "hat");

	public MonkeyHatModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)),
				PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 32);
	}
}
