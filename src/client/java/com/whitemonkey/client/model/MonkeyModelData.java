package com.whitemonkey.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** ARQUIVO GERADO por tools/bbmodel_to_java.py - nao edite a mao; reexporte o .bbmodel. */
public final class MonkeyModelData {
    private MonkeyModelData() {}

    /** Caminho (hierarquia) de cada osso/grupo do Blockbench dentro do ModelPart raiz. */
    public static final String[][] BONE_PATHS = {
        {"root"},
        {"root", "body"},
        {"root", "body", "head"},
        {"root", "body", "left_arm"},
        {"root", "body", "right_arm"},
        {"root", "body", "tail"},
        {"root", "body", "item_eat"},
        {"root", "body", "bone"},
        {"root", "right_leg"},
        {"root", "right_leg", "bone2"},
        {"root", "left_leg"},
        {"root", "left_leg", "bone3"},
    };

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_root = root.addOrReplaceChild("root",
                CubeListBuilder.create(),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body",
                CubeListBuilder.create(),
                PartPose.offset(0F, -3.5F, 2F));
        PartDefinition p_head = p_body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F).texOffs(20, 21).addBox(-2F, -3F, -6F, 4F, 3F, 2F).texOffs(0, 2).addBox(1F, -4.8F, -4.2F, 1.9F, 2.4F, 0F).texOffs(0, 2).addBox(-2.9F, -4.8F, -4.2F, 1.9F, 2.4F, 0F).texOffs(0, 38).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.25F)).texOffs(30, 37).addBox(-2F, -3F, -6F, 4F, 3F, 2F, new CubeDeformation(0.25F)),
                PartPose.offset(0F, -4.5F, -2F));
        PartDefinition p_head_c1 = p_head.addOrReplaceChild("head_c1",
                CubeListBuilder.create().texOffs(32, 25).addBox(-4.88407F, -5F, -1.0828F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_head_c2 = p_head.addOrReplaceChild("head_c2",
                CubeListBuilder.create().texOffs(32, 29).addBox(-5.88407F, -6F, -1.0828F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_head_c3 = p_head.addOrReplaceChild("head_c3",
                CubeListBuilder.create().texOffs(32, 14).addBox(-6.26777F, 1.03935F, -1.0828F, 1.4F, 1F, 1F, new CubeDeformation(-0.001F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.28492F, 0.27401F, 0.82494F));
        PartDefinition p_head_c4 = p_head.addOrReplaceChild("head_c4",
                CubeListBuilder.create().texOffs(36, 14).addBox(0.31941F, -7.16587F, -1.0828F, 1F, 1.5F, 1F, new CubeDeformation(-0.001F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.28492F, 0.27401F, -0.82494F));
        PartDefinition p_head_c5 = p_head.addOrReplaceChild("head_c5",
                CubeListBuilder.create().texOffs(36, 25).addBox(-5.13407F, -6F, -1.0828F, 1F, 1F, 1F, new CubeDeformation(0.001F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_head_c6 = p_head.addOrReplaceChild("head_c6",
                CubeListBuilder.create().texOffs(36, 27).addBox(4.13407F, -6F, -1.0828F, 1F, 1F, 1F, new CubeDeformation(0.001F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.3927F, 0F));
        PartDefinition p_head_c7 = p_head.addOrReplaceChild("head_c7",
                CubeListBuilder.create().texOffs(28, 36).addBox(-1.31941F, -7.16587F, -1.0828F, 1F, 1.5F, 1F, new CubeDeformation(-0.001F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.28492F, -0.27401F, 0.82494F));
        PartDefinition p_head_c8 = p_head.addOrReplaceChild("head_c8",
                CubeListBuilder.create().texOffs(36, 29).addBox(4.86777F, 1.03935F, -1.0828F, 1.4F, 1F, 1F, new CubeDeformation(-0.001F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.28492F, -0.27401F, -0.82494F));
        PartDefinition p_head_c9 = p_head.addOrReplaceChild("head_c9",
                CubeListBuilder.create().texOffs(32, 33).addBox(4.88407F, -6F, -1.0828F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.3927F, 0F));
        PartDefinition p_head_c10 = p_head.addOrReplaceChild("head_c10",
                CubeListBuilder.create().texOffs(24, 36).addBox(3.88407F, -5F, -1.0828F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.3927F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 25).addBox(-1F, -0.88478F, -0.92075F, 2F, 4F, 2F),
                PartPose.offset(-4F, -3.61418F, -3.92597F));
        PartDefinition p_left_arm_c1 = p_left_arm.addOrReplaceChild("left_arm_c1",
                CubeListBuilder.create().texOffs(8, 25).addBox(-1.25F, -1.13478F, -1.17075F, 2.5F, 4.5F, 2.5F),
                PartPose.offsetAndRotation(0F, 3F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_left_arm_c2 = p_left_arm.addOrReplaceChild("left_arm_c2",
                CubeListBuilder.create().texOffs(32, 21).addBox(-1.15F, -0.29289F, -0.29289F, 2.3F, 2F, 2F),
                PartPose.offsetAndRotation(0F, 5.14272F, -3.03064F, -0.3927F, 0F, 0F));
        PartDefinition p_left_arm_c3 = p_left_arm.addOrReplaceChild("left_arm_c3",
                CubeListBuilder.create().texOffs(36, 31).mirror().addBox(-1.25F, -1.13478F, -1.17075F, 2.5F, 4.5F, 2.5F, new CubeDeformation(0.25F)).mirror(false),
                PartPose.offsetAndRotation(0F, 3F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(16, 26).addBox(-1F, -0.88478F, -0.92075F, 2F, 4F, 2F),
                PartPose.offset(4F, -3.61418F, -3.92597F));
        PartDefinition p_right_arm_c1 = p_right_arm.addOrReplaceChild("right_arm_c1",
                CubeListBuilder.create().texOffs(24, 32).addBox(-1.15F, -0.29289F, -0.29289F, 2.3F, 2F, 2F),
                PartPose.offsetAndRotation(0F, 5.14272F, -3.03064F, -0.3927F, 0F, 0F));
        PartDefinition p_right_arm_c2 = p_right_arm.addOrReplaceChild("right_arm_c2",
                CubeListBuilder.create().texOffs(24, 26).addBox(-1.25F, -1.13478F, -1.17075F, 2.5F, 4.5F, 2.5F),
                PartPose.offsetAndRotation(0F, 3F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_right_arm_c3 = p_right_arm.addOrReplaceChild("right_arm_c3",
                CubeListBuilder.create().texOffs(36, 31).addBox(-1.25F, -1.13478F, -1.17075F, 2.5F, 4.5F, 2.5F, new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(0F, 3F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail",
                CubeListBuilder.create(),
                PartPose.offset(0F, -0.5F, 1.5F));
        PartDefinition p_tail_c1 = p_tail.addOrReplaceChild("tail_c1",
                CubeListBuilder.create().texOffs(39, 0).addBox(-0.5F, -0.47668F, 0.12F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.61087F, 0F, 0F));
        PartDefinition p_tail_c2 = p_tail.addOrReplaceChild("tail_c2",
                CubeListBuilder.create().texOffs(38, 5).addBox(-0.75F, -0.3742F, 0.03488F, 1.5F, 1.5F, 4F),
                PartPose.offsetAndRotation(0F, -1.75F, 2.75F, 1.22173F, 0F, 0F));
        PartDefinition p_tail_c3 = p_tail.addOrReplaceChild("tail_c3",
                CubeListBuilder.create().texOffs(40, 20).addBox(-1F, -1.82972F, -0.19223F, 2F, 2F, 5F),
                PartPose.offsetAndRotation(0F, -4.56954F, 5.29944F, 1.7017F, 0F, 0F));
        PartDefinition p_tail_c4 = p_tail.addOrReplaceChild("tail_c4",
                CubeListBuilder.create().texOffs(40, 27).addBox(-1F, -3.5303F, 3.74159F, 2F, 2F, 2F, new CubeDeformation(-0.001F)),
                PartPose.offsetAndRotation(0F, -4.56954F, 5.29944F, 1.309F, 0F, 0F));
        PartDefinition p_tail_c5 = p_tail.addOrReplaceChild("tail_c5",
                CubeListBuilder.create().texOffs(48, 27).addBox(-1F, -6.55622F, 1.56362F, 2F, 2F, 2F),
                PartPose.offsetAndRotation(0F, -4.56954F, 5.29944F, 0.5236F, 0F, 0F));
        PartDefinition p_item_eat = p_body.addOrReplaceChild("item_eat",
                CubeListBuilder.create(),
                PartPose.offset(0F, 3.5F, -2F));
        PartDefinition p_bone = p_body.addOrReplaceChild("bone",
                CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bone_c1 = p_bone.addOrReplaceChild("bone_c1",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3F, -5.34357F, -2.2409F, 6F, 5F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.3927F, 0F, 0F));
        PartDefinition p_bone_c2 = p_bone.addOrReplaceChild("bone_c2",
                CubeListBuilder.create().texOffs(20, 16).addBox(-2.5F, -0.57141F, -2.3017F, 5F, 1F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.2618F, 0F, 0F));
        PartDefinition p_right_leg = p_root.addOrReplaceChild("right_leg",
                CubeListBuilder.create(),
                PartPose.offset(4F, -3.11418F, 3.82403F));
        PartDefinition p_right_leg_c1 = p_right_leg.addOrReplaceChild("right_leg_c1",
                CubeListBuilder.create().texOffs(0, 31).addBox(-1F, -3.09992F, -0.84866F, 2F, 4F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.7854F, -0.3927F, 0F));
        PartDefinition p_bone2 = p_right_leg.addOrReplaceChild("bone2",
                CubeListBuilder.create().texOffs(32, 0).addBox(9.25067F, 3.27654F, -3.37684F, 2.3F, 1F, 3F),
                PartPose.offset(-8.90067F, -1.43934F, -1.6744F));
        PartDefinition p_bone2_c1 = p_bone2.addOrReplaceChild("bone2_c1",
                CubeListBuilder.create().texOffs(8, 31).addBox(-1.25F, -1.29329F, -3.40119F, 2.5F, 4.5F, 2.5F),
                PartPose.offsetAndRotation(8.90067F, 1.43934F, 1.6744F, -0.3927F, -0.3927F, 0F));
        PartDefinition p_bone2_c2 = p_bone2.addOrReplaceChild("bone2_c2",
                CubeListBuilder.create().texOffs(38, 14).addBox(-1.25F, -1.29329F, -3.40119F, 2.5F, 4.5F, 2.5F, new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(8.90067F, 1.43934F, 1.6744F, -0.3927F, -0.3927F, 0F));
        PartDefinition p_left_leg = p_root.addOrReplaceChild("left_leg",
                CubeListBuilder.create(),
                PartPose.offset(-4F, -3.11418F, 3.82403F));
        PartDefinition p_left_leg_c1 = p_left_leg.addOrReplaceChild("left_leg_c1",
                CubeListBuilder.create().texOffs(16, 32).addBox(-1F, -3.09992F, -0.84866F, 2F, 4F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.7854F, 0.3927F, 0F));
        PartDefinition p_bone3 = p_left_leg.addOrReplaceChild("bone3",
                CubeListBuilder.create().texOffs(32, 4).addBox(-1.55067F, 3.27654F, -3.37684F, 2.3F, 1F, 3F),
                PartPose.offset(-1.09933F, -1.43934F, -1.6744F));
        PartDefinition p_bone3_c1 = p_bone3.addOrReplaceChild("bone3_c1",
                CubeListBuilder.create().texOffs(38, 14).mirror().addBox(-1.05866F, -0.6874F, -1.10276F, 2.5F, 4.5F, 2.5F, new CubeDeformation(0.25F)).mirror(false),
                PartPose.offsetAndRotation(0.19866F, 0F, 0F, -0.3927F, 0.3927F, 0F));
        PartDefinition p_bone3_c2 = p_bone3.addOrReplaceChild("bone3_c2",
                CubeListBuilder.create().texOffs(32, 8).addBox(-1.25F, -1.29329F, -3.40119F, 2.5F, 4.5F, 2.5F),
                PartPose.offsetAndRotation(1.09933F, 1.43934F, 1.6744F, -0.3927F, 0.3927F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
