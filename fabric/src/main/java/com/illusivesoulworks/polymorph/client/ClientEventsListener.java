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
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;

public class ClientEventsListener {

  public static void setup() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> PolymorphClientEvents.tick());
    ClientChunkEvents.CHUNK_UNLOAD.register(
        (level, chunk) -> ClientInWorldStates.chunkUnloaded(chunk.getPos()));
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> client.execute(ClientInWorldStates::clear));
    WorldRenderEvents.AFTER_ENTITIES.register(context -> InWorldClient.render(
        context.matrixStack(), Minecraft.getInstance().renderBuffers().bufferSource(),
        context.camera()));
    AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
        level.isClientSide() && player instanceof LocalPlayer localPlayer &&
            InWorldClient.attack(localPlayer, pos) ? InteractionResult.FAIL :
            InteractionResult.PASS);
    ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
      PolymorphClientEvents.initScreen(screen);
      ScreenEvents.afterRender(screen).register(PolymorphClientEvents::renderIfMissed);
      // allowMouseClick / allowMouseScroll return true to allow, false to block. Our
      // mouseClick / mouseScroll return true when the polymorph widget consumed it
      // (vanilla should be blocked). Invert.
      ScreenMouseEvents.allowMouseClick(screen).register((s, mx, my, btn) ->
          !PolymorphClientEvents.mouseClick(s, mx, my, btn));
      ScreenMouseEvents.allowMouseScroll(screen).register((s, mx, my, hAmt, vAmt) ->
          !PolymorphClientEvents.mouseScroll(s, mx, my, vAmt));
    });
  }
}
