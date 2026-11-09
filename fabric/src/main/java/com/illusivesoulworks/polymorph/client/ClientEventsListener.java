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

import com.illusivesoulworks.polymorph.client.inworld.ClientInWorldStates;
import com.illusivesoulworks.polymorph.client.inworld.InWorldClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;

public class ClientEventsListener {

  public static void setup() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> PolymorphClientEvents.tick());
    ClientChunkEvents.CHUNK_UNLOAD.register(
        (level, chunk) -> ClientInWorldStates.chunkUnloaded(chunk.getPos()));
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> client.execute(ClientInWorldStates::clear));
    LevelRenderEvents.COLLECT_SUBMITS.register(context -> InWorldClient.render(
        context.poseStack(), context.submitNodeCollector(),
        context.levelState().cameraRenderState));
    AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
        level.isClientSide() && player instanceof LocalPlayer localPlayer &&
            InWorldClient.attack(localPlayer, pos) ? InteractionResult.FAIL :
            InteractionResult.PASS);
    ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
      PolymorphClientEvents.initScreen(screen);
      ScreenEvents.afterExtract(screen).register(PolymorphClientEvents::renderIfMissed);
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
