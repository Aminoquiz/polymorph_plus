package com.illusivesoulworks.polymorph.common.inworld;

import com.illusivesoulworks.polymorph.api.inworld.IndicatorSpot;
import com.illusivesoulworks.polymorph.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class InWorldHit {

  private InWorldHit() {
  }

  /** Quarter turns from the spot's frame to the world, as the renderer applies them. */
  public static int facingSteps(IndicatorSpot spot, BlockState state) {
    int facing =
        spot.followsFacing() && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? state.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue() : 0;
    return (facing + spot.turns()) & 3;
  }

  /**
   * The icon can float over a hollow (a casting table's cast), so the point where the view
   * ray meets the block may sit on an inner wall below it. The ray is met with the spot's
   * plane instead.
   */
  public static boolean isOnSpot(Player player, BlockPos pos, BlockState state,
                                 IndicatorSpot spot, HitResult result) {

    if (!(result instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK ||
        !hit.getBlockPos().equals(pos)) {
      return false;
    }
    Vec3 eye = player.getEyePosition();
    Vec3 view = player.getViewVector(1.0F);

    if (spot.frontFace()) {
      return isOnFrontFace(eye, view, pos, state, spot);
    }
    double planeY = pos.getY() + spot.height();

    if (view.y >= 0.0 || eye.y <= planeY) {
      return false;
    }
    double t = (planeY - eye.y) / view.y;
    double x = eye.x + view.x * t - pos.getX() - 0.5;
    double z = eye.z + view.z * t - pos.getZ() - 0.5;
    double angle = Math.toRadians(90.0 * facingSteps(spot, state));
    double localX = x * Math.cos(angle) + z * Math.sin(angle) + 0.5;
    double localZ = -x * Math.sin(angle) + z * Math.cos(angle) + 0.5;
    return Math.abs(localX - spot.u()) <= spot.halfSize() &&
        Math.abs(localZ - spot.v()) <= spot.halfSize();
  }

  private static boolean isOnFrontFace(Vec3 eye, Vec3 view, BlockPos pos, BlockState state,
                                       IndicatorSpot spot) {
    double angle = Math.toRadians(90.0 * facingSteps(spot, state));
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    double ex = eye.x - pos.getX() - 0.5;
    double ez = eye.z - pos.getZ() - 0.5;
    double eyeX = ex * cos + ez * sin;
    double eyeZ = -ex * sin + ez * cos;
    double viewX = view.x * cos + view.z * sin;
    double viewZ = -view.x * sin + view.z * cos;

    if (viewZ >= 0.0 || eyeZ <= 0.5) {
      return false;
    }
    double t = (0.5 - eyeZ) / viewZ;
    double localX = eyeX + viewX * t + 0.5;
    double y = eye.y + view.y * t - pos.getY();
    return Math.abs(localX - spot.u()) <= spot.halfSize() &&
        Math.abs(y - spot.height()) <= spot.halfSize();
  }

  public static boolean isLookingAtSpot(Player player, BlockPos pos, BlockState state,
                                        IndicatorSpot spot) {
    return isOnSpot(player, pos, state, spot,
        player.pick(Services.PLATFORM.getBlockReach(player), 1.0F, false));
  }
}
