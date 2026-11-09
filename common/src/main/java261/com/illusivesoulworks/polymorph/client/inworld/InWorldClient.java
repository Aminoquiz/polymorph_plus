package com.illusivesoulworks.polymorph.client.inworld;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.inworld.IInWorldProvider;
import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.client.PolymorphClientConfig;
import com.illusivesoulworks.polymorph.common.inworld.InWorldHit;
import com.illusivesoulworks.polymorph.common.inworld.InWorldServer;
import com.illusivesoulworks.polymorph.common.inworld.InWorldState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class InWorldClient {

  private static final double MAX_DISTANCE_SQR = 32.0 * 32.0;
  private static final RenderType BADGE = RenderTypes.entityCutout(
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "textures/inworld/badge.png"));
  private static final RenderType FRAME = RenderTypes.entityCutout(
      Identifier.fromNamespaceAndPath(PolymorphApi.MOD_ID, "textures/inworld/frame.png"));

  private InWorldClient() {
  }

  /**
   * Called from the loader's attack-block hook on the client. Returns true when the hit lands
   * on an indicator: the caller cancels mining and the server cycles the choice.
   */
  public static boolean attack(LocalPlayer player, BlockPos pos) {

    int index =
        indicatorAt(player, pos, player.pick(player.blockInteractionRange(), 1.0F, false));

    if (index < 0) {
      return false;
    }
    PolymorphApi.getInstance().getNetwork().sendInWorldCycleC2S(pos, index);
    return true;
  }

  /** The index of the indicator the hit lands on, or -1. */
  private static int indicatorAt(LocalPlayer player, BlockPos pos, @Nullable HitResult result) {
    Map<Integer, InWorldState> shown = ClientInWorldStates.at(pos);

    if (!player.getMainHandItem().isEmpty() || shown == null || result == null) {
      return -1;
    }
    BlockState state = player.level().getBlockState(pos);
    IInWorldProvider provider = InWorldServer.provider(state.getBlock());

    if (provider != null) {

      for (int index : shown.keySet()) {

        if (InWorldHit.isOnSpot(player, pos, state, provider.spot(state, index), result)) {
          return index;
        }
      }
    }
    return -1;
  }

  /**
   * Submits the indicators during the level's submit collection; the pose stack is in camera
   * space, as the loaders hand it over.
   */
  public static void render(PoseStack poseStack, SubmitNodeCollector collector,
                            CameraRenderState camera) {
    Minecraft minecraft = Minecraft.getInstance();
    ClientLevel level = minecraft.level;
    LocalPlayer player = minecraft.player;

    if (level == null || player == null || !PolymorphClientConfig.isInWorldIndicators()) {
      return;
    }
    Map<BlockPos, Map<Integer, InWorldState>> states = ClientInWorldStates.all();

    if (states.isEmpty()) {
      return;
    }
    BlockPos hovered = null;
    int hoveredIndex = -1;

    if (minecraft.hitResult instanceof BlockHitResult hit) {
      hoveredIndex = indicatorAt(player, hit.getBlockPos(), hit);
      hovered = hoveredIndex < 0 ? null : hit.getBlockPos();
    }
    Vec3 cam = camera.pos;

    for (Map.Entry<BlockPos, Map<Integer, InWorldState>> entry : states.entrySet()) {
      BlockPos pos = entry.getKey();

      if (Vec3.atCenterOf(pos).distanceToSqr(cam) > MAX_DISTANCE_SQR || !level.isLoaded(pos)) {
        continue;
      }
      BlockState state = level.getBlockState(pos);
      IInWorldProvider provider = InWorldServer.provider(state.getBlock());

      if (provider == null) {
        continue;
      }

      for (Map.Entry<Integer, InWorldState> shown : entry.getValue().entrySet()) {
        int index = shown.getKey();
        poseStack.pushPose();
        poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
        renderIndicator(minecraft, level, poseStack, collector, camera, pos, state,
            provider.spot(state, index), shown.getValue(),
            pos.equals(hovered) && index == hoveredIndex);
        poseStack.popPose();
      }
    }
  }

  private static void renderIndicator(Minecraft minecraft, ClientLevel level,
                                      PoseStack poseStack, SubmitNodeCollector collector,
                                      CameraRenderState camera, BlockPos pos, BlockState state,
                                      IndicatorSpot spot, InWorldState indicator,
                                      boolean hovered) {

    if (hovered) {
      renderHint(minecraft, poseStack, collector, camera, spot.height() + 0.45F);
    }
    // A solid block is dark inside, so the light comes from the space the icon sits in.
    int steps = InWorldHit.facingSteps(spot, state);
    BlockPos lightPos = spot.frontFace()
        ? pos.relative(Direction.from2DDataValue(steps))
        : spot.height() >= 1.0F ? pos.above() : pos;
    int light = LightCoordsUtil.getLightCoords(level, lightPos);
    float half = spot.halfSize();
    poseStack.pushPose();
    poseStack.translate(0.5F, 0.0F, 0.5F);
    poseStack.rotateDegrees(Axis.YP, -90.0F * steps);

    if (spot.frontFace()) {
      poseStack.translate(spot.u() - 0.5F, spot.height(), 0.505F);
    } else {
      poseStack.translate(spot.u() - 0.5F, spot.height() + 0.005F, spot.v() - 0.5F);
      poseStack.rotateDegrees(Axis.XP, -90.0F);
    }
    poseStack.pushPose();
    poseStack.scale(0.25F, 0.25F, 0.25F);
    // The FIXED transform turns the model 180 degrees on Y, which leaves its lit face pointing
    // away from the viewer; turning it back puts that face, and its normal, toward them.
    poseStack.rotateDegrees(Axis.YP, 180.0F);
    // The submitted nodes keep references into the state until the frame draws, so each
    // indicator needs its own.
    ItemStackRenderState itemState = new ItemStackRenderState();
    minecraft.getItemModelResolver().updateForTopItem(itemState,
        indicator.options().get(indicator.shown()).icon(), ItemDisplayContext.FIXED, level, null,
        (int) pos.asLong() + 2);
    itemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
    poseStack.popPose();

    if (hovered) {
      collector.submitCustomGeometry(poseStack, FRAME,
          (pose, consumer) -> quad(consumer, pose, -half, -half, half, half, light));
    }
    // The badge tells a choice apart from an item the block is working on.
    collector.submitCustomGeometry(poseStack, BADGE,
        (pose, consumer) -> quad(consumer, pose, half * 0.125F, -half * 0.8125F,
            half * 0.8125F, -half * 0.125F, light));
    poseStack.popPose();
  }

  private static void renderHint(Minecraft minecraft, PoseStack poseStack,
                                 SubmitNodeCollector collector, CameraRenderState camera,
                                 float y) {
    FormattedCharSequence hint = Component.translatable("polymorph.inworld.hint",
        minecraft.options.keyAttack.getTranslatedKeyMessage()).getVisualOrderText();
    Font font = minecraft.font;
    poseStack.pushPose();
    poseStack.translate(0.5F, y, 0.5F);
    poseStack.rotate(camera.orientation);
    poseStack.scale(0.02F, -0.02F, 0.02F);
    collector.submitText(poseStack, -font.width(hint) / 2.0F, 0.0F, hint, false,
        Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, 0xFFFFFFFF, 0x40000000, 0);
    poseStack.popPose();
  }

  private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float x0, float y0,
                           float x1, float y1, int light) {
    vertex(consumer, pose, x0, y0, 0.0F, 1.0F, light);
    vertex(consumer, pose, x1, y0, 1.0F, 1.0F, light);
    vertex(consumer, pose, x1, y1, 1.0F, 0.0F, light);
    vertex(consumer, pose, x0, y1, 0.0F, 0.0F, light);
  }

  private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y,
                             float u, float v, int light) {
    consumer.addVertex(pose, x, y, 0.03F)
        .setColor(255, 255, 255, 255)
        .setUv(u, v)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(light)
        .setNormal(pose, 0.0F, 0.0F, 1.0F);
  }
}
