package com.illusivesoulworks.polymorph.common.priority;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RecipePriorityTest {

  @AfterEach
  void reset() {
    RecipePriority.setPackNamespaces(java.util.Collections.emptyList());
    RecipePriority.setClientPackNamespaces(java.util.Collections.emptyList());
  }

  @Test
  void clientClearKeepsServerList() {
    RecipePriority.setPackNamespaces(java.util.Arrays.asList("zzz_test", "minecraft"));
    RecipePriority.setClientPackNamespaces(java.util.Collections.emptyList());

    assertEquals(0, RecipePriority.packRank("zzz_test"));
    assertEquals(1, RecipePriority.packRank("minecraft"));
    assertEquals(Integer.MAX_VALUE, RecipePriority.packRank("aaa_test"));
    assertEquals(java.util.Arrays.asList("zzz_test", "minecraft"), RecipePriority.getPackNamespaces());
  }

  @Test
  void sidesRankIndependently() {
    RecipePriority.setPackNamespaces(java.util.Arrays.asList("zzz_test"));
    RecipePriority.setClientPackNamespaces(java.util.Arrays.asList("aaa_test", "zzz_test"));

    assertEquals(0, RecipePriority.packRank("zzz_test", false));
    assertEquals(1, RecipePriority.packRank("zzz_test", true));
    assertEquals(Integer.MAX_VALUE, RecipePriority.packRank("aaa_test", false));
    assertEquals(0, RecipePriority.packRank("aaa_test", true));
  }

  @Test
  void parseAcceptsObjectAndArray() {
    assertEquals(java.util.Arrays.asList("zzz_test", "minecraft"),
        RecipePriority.parse("{\"namespaces\":[\"zzz_test\",\"minecraft\",\"zzz_test\"]}"));
    assertEquals(java.util.Arrays.asList("a"), RecipePriority.parse("[\"a\", \" \"]"));
    assertEquals(java.util.Collections.emptyList(), RecipePriority.parse("{not json"));
  }
}
