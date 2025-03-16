package net.turtleboi.aspects.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.turtleboi.aspects.Aspects;

public class PedestalModel extends Model {
    public static final ModelLayerLocation PEDESTAL_LAYER = new ModelLayerLocation(new ResourceLocation(Aspects.MOD_ID, "pedestal"), "main");
    private final ModelPart mainPart;

    public PedestalModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.mainPart = root.getChild("mainPart");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition cubeMain = partdefinition.addOrReplaceChild(
                "mainPart",
                CubeListBuilder.create()
                        .texOffs(-21, -14)
                        .addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        mainPart.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }
}
