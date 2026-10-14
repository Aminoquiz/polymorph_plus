/*
 * Copyright (C) 2020-2026 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License v3.0-or-later.
 */

package com.illusivesoulworks.polymorph.common.network.server;

import com.illusivesoulworks.polymorph.common.priority.RecipePriority;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

/** Pushes the datapack namespace priority list so the client ranks conflicts like the server. */
public final class SPacketPackPriority {
  private final List<String> namespaces;

  public SPacketPackPriority(List<String> namespaces) {
    this.namespaces = namespaces;
  }

  public List<String> namespaces() {
    return this.namespaces;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SPacketPackPriority)) {
      return false;
    }
    SPacketPackPriority other = (SPacketPackPriority) o;
    return java.util.Objects.equals(this.namespaces, other.namespaces);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(this.namespaces);
  }


  public static final int MAX_ENTRIES = 256;
  public static final int MAX_LENGTH = 64;

  public static void encode(SPacketPackPriority packet, FriendlyByteBuf buffer) {
    int size = Math.min(packet.namespaces.size(), MAX_ENTRIES);
    buffer.writeVarInt(size);

    for (int i = 0; i < size; i++) {
      buffer.writeUtf(packet.namespaces.get(i), MAX_LENGTH);
    }
  }

  public static SPacketPackPriority decode(FriendlyByteBuf buffer) {
    int size = buffer.readVarInt();

    if (size < 0 || size > MAX_ENTRIES) {
      throw new IllegalArgumentException("Priority list too long: " + size);
    }
    List<String> list = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      list.add(buffer.readUtf(MAX_LENGTH));
    }
    return new SPacketPackPriority(list);
  }

  public static void handle(SPacketPackPriority packet) {
    RecipePriority.setClientPackNamespaces(packet.namespaces);
  }
}
