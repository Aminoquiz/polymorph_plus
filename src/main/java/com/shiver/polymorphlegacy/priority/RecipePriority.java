package com.shiver.polymorphlegacy.priority;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The server's source-priority list. 1.12 has no datapacks, so the list comes from
 * {@code config/polymorph/priority.json} first, then from {@code data/polymorph_plus/priority.json}
 * or {@code data/polymorph/priority.json} inside any mod jar; see {@code PriorityLoader}.
 */
public final class RecipePriority {
    private static volatile List<String> namespaces = Collections.emptyList();
    private static volatile Map<String, Integer> ranks = Collections.emptyMap();

    private RecipePriority() {
    }

    public static void setPackNamespaces(List<String> list) {
        List<String> copy = Collections.unmodifiableList(new ArrayList<>(list));
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < copy.size(); i++) {
            map.putIfAbsent(copy.get(i), i);
        }
        ranks = Collections.unmodifiableMap(map);
        namespaces = copy;
    }

    public static List<String> getPackNamespaces() {
        return namespaces;
    }

    /** Lower wins; {@link Integer#MAX_VALUE} means the namespace is not listed. */
    public static int packRank(String namespace) {
        Integer rank = ranks.get(namespace);
        return rank == null ? Integer.MAX_VALUE : rank;
    }

    /**
     * Accepts {@code {"namespaces": [...]}} or a bare array. Malformed input gives an empty
     * list: a broken file must not stop a server, it only turns automatic resolution off.
     */
    public static List<String> parse(String json) {
        List<String> result = new ArrayList<>();
        try {
            JsonElement root = new JsonParser().parse(json);
            JsonArray array = null;
            if (root.isJsonArray()) {
                array = root.getAsJsonArray();
            } else if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                if (obj.has("namespaces") && obj.get("namespaces").isJsonArray()) {
                    array = obj.getAsJsonArray("namespaces");
                }
            }
            if (array != null) {
                for (JsonElement element : array) {
                    if (element.isJsonPrimitive()) {
                        String value = element.getAsString().trim();
                        if (!value.isEmpty() && !result.contains(value)) {
                            result.add(value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return result;
    }

    /** The first source sets the order; later sources append what it did not name. */
    public static List<String> merge(List<List<String>> sources) {
        Set<String> merged = new LinkedHashSet<>();
        for (List<String> source : sources) {
            merged.addAll(source);
        }
        return new ArrayList<>(merged);
    }
}
