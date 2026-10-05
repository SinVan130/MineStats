package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class TitleNameTags {

    private static final double TITLE_Y_OFFSET = 0.27;  // высота строки титула, если сдвинулась, подправь
    private static final float TITLE_SCALE = 0.02f;     // размер текста (у ника 0.025)

    @SubscribeEvent
    public static void onNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !Minecraft.renderNames()) return;
        if (player == mc.player) return;
        if (player.isDiscrete() || player.isInvisibleTo(mc.player)) return;
        if (mc.getEntityRenderDispatcher().distanceToSqr(player) > 4096.0) return;

        Titles.Def def = TitleEvents.selected(player);
        if (def == null) return;

        Component text = def.styledName();
        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0.0, player.getBbHeight() + TITLE_Y_OFFSET, 0.0);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(TITLE_SCALE, -TITLE_SCALE, TITLE_SCALE);
        var matrix = poseStack.last().pose();
        int background = (int) (mc.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;
        float x = -mc.font.width(text) / 2.0f;
        mc.font.drawInBatch(text, x, 0.0f, -1, false, matrix, event.getMultiBufferSource(),
                Font.DisplayMode.NORMAL, background, event.getPackedLight());
        poseStack.popPose();
    }
}