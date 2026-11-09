package com.illusivesoulworks.polymorph.api.inworld;

/**
 * Where the indicator sits. By default it lies on the top face: a square of {@code halfSize}
 * centred at ({@code u}, {@code v}) in the block frame, on the plane {@code y + height}. When
 * {@code followsFacing} is true, the frame turns with the block's horizontal facing, with
 * (u, v) given for a block facing south, the frame Mantle's
 * {@code RenderingHelper.applyRotation} leaves unrotated.
 * <p>
 * A {@code frontFace} spot stands upright on the face toward the facing instead (z = 1 in the
 * south frame), centred at {@code u} across and {@code height} up; {@code v} is unused.
 * <p>
 * {@code turns} adds quarter turns on top of the facing, the way the block's renderer turns
 * from one slot to the next.
 */
public record IndicatorSpot(float u, float v, float halfSize, float height,
                            boolean followsFacing, boolean frontFace, int turns) {

  public IndicatorSpot(float u, float v, float halfSize, float height, boolean followsFacing,
                       boolean frontFace) {
    this(u, v, halfSize, height, followsFacing, frontFace, 0);
  }

  public IndicatorSpot(float u, float v, float halfSize, float height, boolean followsFacing) {
    this(u, v, halfSize, height, followsFacing, false);
  }

  /** {@code u} runs left to right as seen from the front, {@code height} from the bottom. */
  public static IndicatorSpot onFrontFace(float u, float height, float halfSize) {
    return new IndicatorSpot(u, 1.0f, halfSize, height, true, true);
  }

  public IndicatorSpot turned(int quarterTurns) {
    return new IndicatorSpot(u, v, halfSize, height, followsFacing, frontFace,
        turns + quarterTurns);
  }
}
