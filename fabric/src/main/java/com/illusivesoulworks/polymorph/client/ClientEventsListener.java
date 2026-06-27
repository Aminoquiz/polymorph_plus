/*
 * Copyright (C) 2020-2022 Illusive Soulworks
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

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

public class ClientEventsListener {

  public static void setup() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> PolymorphClientEvents.tick());
    ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
      PolymorphClientEvents.initScreen(screen);
      ScreenEvents.afterRender(screen).register(PolymorphClientEvents::render);
      // allowMouseClick returns true to ALLOW the click, false to BLOCK. Our mouseClick
      // returns true when our overlay CONSUMED the click (vanilla should be blocked).
      // Invert: block vanilla only when consumed; otherwise let the click through.
      ScreenMouseEvents.allowMouseClick(screen).register((s, event) ->
          !PolymorphClientEvents.mouseClick(s, event.x(), event.y(), event.button()));
      ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, hAmt, vAmt) ->
          !PolymorphClientEvents.mouseScroll(s, mouseX, mouseY, vAmt));
    });
  }
}
