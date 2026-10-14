package com.illusivesoulworks.polymorph.common.priority;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RecipePriorityTest {

  @AfterEach
  void reset() {
    RecipePriority.setPackNamespaces(List.of());
    RecipePriority.setClientPackNamespaces(List.of());
  }

  @Test
  void clientClearKeepsServerList() {
    RecipePriority.setPackNamespaces(List.of("zzz_test", "minecraft"));
    RecipePriority.setClientPackNamespaces(List.of());

    assertEquals(0, RecipePriority.packRank("zzz_test"));
    assertEquals(1, RecipePriority.packRank("minecraft"));
    assertEquals(Integer.MAX_VALUE, RecipePriority.packRank("aaa_test"));
    assertEquals(List.of("zzz_test", "minecraft"), RecipePriority.getPackNamespaces());
  }

  @Test
  void sidesRankIndependently() {
    RecipePriority.setPackNamespaces(List.of("zzz_test"));
    RecipePriority.setClientPackNamespaces(List.of("aaa_test", "zzz_test"));

    assertEquals(0, RecipePriority.packRank("zzz_test", false));
    assertEquals(1, RecipePriority.packRank("zzz_test", true));
    assertEquals(Integer.MAX_VALUE, RecipePriority.packRank("aaa_test", false));
    assertEquals(0, RecipePriority.packRank("aaa_test", true));
  }

  @Test
  void parseAcceptsObjectAndArray() {
    assertEquals(List.of("zzz_test", "minecraft"),
        RecipePriority.parse("{\"namespaces\":[\"zzz_test\",\"minecraft\",\"zzz_test\"]}"));
    assertEquals(List.of("a"), RecipePriority.parse("[\"a\", \" \"]"));
    assertEquals(List.of(), RecipePriority.parse("{not json"));
  }
}
