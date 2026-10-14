package com.shiver.polymorphlegacy.priority;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RecipeRankingTest {
    private static final List<String> CANDIDATES = Arrays.asList(
            "aaa_test:diamond_to_blaze_powder", "zzz_test:diamond_to_emerald",
            "minecraft:diamond_to_coal", "zzz_test:diamond_to_gold");

    @AfterEach
    void reset() {
        RecipePriority.setPackNamespaces(Collections.<String>emptyList());
    }

    private static RecipeRanking ranking(List<String> favourites, List<String> remembered,
            List<String> namespaces) {
        return new RecipeRanking(favourites, remembered, namespaces);
    }

    @Test
    void firstCandidateWithoutPreferences() {
        assertEquals(0, RecipeRanking.NONE.choose(CANDIDATES));
        assertEquals(-1, RecipeRanking.NONE.choose(Collections.<String>emptyList()));
    }

    @Test
    void packListPicksHighestListedNamespace() {
        RecipePriority.setPackNamespaces(Arrays.asList("zzz_test", "minecraft"));
        assertEquals(1, RecipeRanking.NONE.choose(CANDIDATES));
    }

    @Test
    void playerNamespacesOutrankPackList() {
        RecipePriority.setPackNamespaces(Arrays.asList("zzz_test"));
        RecipeRanking ranking = ranking(Collections.<String>emptyList(),
                Collections.<String>emptyList(), Arrays.asList("minecraft"));
        assertEquals(2, ranking.choose(CANDIDATES));
    }

    @Test
    void rememberedPickOutranksNamespaces() {
        RecipePriority.setPackNamespaces(Arrays.asList("zzz_test"));
        RecipeRanking ranking = ranking(Collections.<String>emptyList(),
                Arrays.asList("other:recipe", "zzz_test:diamond_to_gold"), Arrays.asList("minecraft"));
        assertEquals(3, ranking.choose(CANDIDATES));
    }

    @Test
    void favouriteOutranksRememberedPick() {
        RecipeRanking ranking = ranking(Arrays.asList("minecraft:diamond_to_coal"),
                Arrays.asList("zzz_test:diamond_to_gold"), Collections.<String>emptyList());
        assertEquals(2, ranking.choose(CANDIDATES));
    }

    @Test
    void lowerFavouriteRankWins() {
        RecipeRanking ranking = ranking(Arrays.asList("zzz_test:diamond_to_gold",
                "aaa_test:diamond_to_blaze_powder"), Collections.<String>emptyList(),
                Collections.<String>emptyList());
        assertEquals(3, ranking.choose(CANDIDATES));
    }

    @Test
    void parseAndMerge() {
        assertEquals(Arrays.asList("zzz_test", "minecraft"),
                RecipePriority.parse("{\"namespaces\":[\"zzz_test\",\"minecraft\",\"zzz_test\"]}"));
        assertEquals(Arrays.asList("a"), RecipePriority.parse("[\"a\", \" \"]"));
        assertEquals(Collections.<String>emptyList(), RecipePriority.parse("{not json"));
        assertEquals(Arrays.asList("b", "a", "c"), RecipePriority.merge(Arrays.asList(
                Arrays.asList("b", "a"), Arrays.asList("a", "c"))));
    }
}
