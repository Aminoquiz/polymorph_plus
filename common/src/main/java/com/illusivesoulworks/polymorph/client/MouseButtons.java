/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.polymorph.client;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * The mouse button codes of the version that is actually running.
 *
 * <p>{@code InputConstants.MOUSE_BUTTON_LEFT} and {@code MOUSE_BUTTON_RIGHT} are compile-time
 * constants: javac copies their value into the bytecode of whatever references them. A jar built
 * against a version that numbered the buttons 0 (left) and 1 (right) therefore still carries
 * those numbers when it runs on a version that renumbered them, which is what 26.3 did: left,
 * middle and right are now 1, 2 and 3. Resolving the keys by name is a runtime lookup, so it
 * returns the codes of the running game either way.
 */
public final class MouseButtons {

  public static final int LEFT = InputConstants.getKey("key.mouse.left").getValue();
  public static final int RIGHT = InputConstants.getKey("key.mouse.right").getValue();

  private MouseButtons() {
  }
}
