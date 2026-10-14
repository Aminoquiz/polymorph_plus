package com.shiver.polymorphlegacy.priority;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Picks the default among conflicting recipes when the open menu holds no click of its own.
 * Order: the player's favourites, then the picks they made in earlier menus, then their own
 * namespace list, then the server's list, then the first candidate.
 */
public final class RecipeRanking {
    public static final RecipeRanking NONE = new RecipeRanking(Collections.<String>emptyList(),
            Collections.<String>emptyList(), Collections.<String>emptyList());
    private static final int PACK_RANK_OFFSET = 1 << 20;

    private final Map<String, Integer> favourites;
    private final Map<String, Integer> remembered;
    private final Map<String, Integer> namespaces;

    public RecipeRanking(List<String> favourites, List<String> remembered, List<String> namespaces) {
        this.favourites = ranks(favourites);
        this.remembered = ranks(remembered);
        this.namespaces = ranks(namespaces);
    }

    private static Map<String, Integer> ranks(List<String> list) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            map.putIfAbsent(list.get(i), i);
        }
        return map;
    }

    private static int rank(Map<String, Integer> ranks, String key) {
        Integer rank = ranks.get(key);
        return rank == null ? Integer.MAX_VALUE : rank;
    }

    /** Recipe ids as {@code namespace:path}. Returns -1 only for an empty list. */
    public int choose(List<String> candidates) {
        if (candidates.isEmpty()) {
            return -1;
        }
        int index = best(candidates, favourites, false);
        if (index < 0) {
            index = best(candidates, remembered, false);
        }
        if (index < 0) {
            index = best(candidates, namespaces, true);
        }
        return index < 0 ? 0 : index;
    }

    private static int best(List<String> candidates, Map<String, Integer> ranks, boolean bySource) {
        int best = -1;
        int bestRank = Integer.MAX_VALUE;
        for (int i = 0; i < candidates.size(); i++) {
            String id = candidates.get(i);
            int rank;
            if (bySource) {
                String namespace = namespaceOf(id);
                rank = rank(ranks, namespace);
                if (rank == Integer.MAX_VALUE) {
                    int pack = RecipePriority.packRank(namespace);
                    rank = pack == Integer.MAX_VALUE ? Integer.MAX_VALUE : PACK_RANK_OFFSET + pack;
                }
            } else {
                rank = rank(ranks, id);
            }
            if (rank < bestRank) {
                bestRank = rank;
                best = i;
            }
        }
        return best;
    }

    static String namespaceOf(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? "minecraft" : id.substring(0, colon);
    }
}
