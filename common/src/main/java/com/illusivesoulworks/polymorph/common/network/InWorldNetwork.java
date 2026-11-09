package com.illusivesoulworks.polymorph.common.network;

public final class InWorldNetwork {

  // Mod version (major_minor) where the in-world payload format last changed. Bump only when
  // these packets change, so mismatched peers never receive a format they cannot read.
  public static final String FORMAT = "1_5";

  private InWorldNetwork() {
  }
}
