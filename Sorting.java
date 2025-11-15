package com.slginventory.algorithms;

import com.slginventory.model.InventoryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Sorting {
    public static List<InventoryItem> sortByQuantityDesc(List<InventoryItem> items) {
        List<InventoryItem> copy = new ArrayList<>(items);
        copy.sort(Comparator.comparingInt(InventoryItem::getQuantity).reversed()
                .thenComparing(o -> o.getGoods().getName()));
        return copy;
    }

    public static <T> List<T> stableByStringKey(List<T> list, java.util.function.Function<T, String> key) {
        List<T> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(key));
        return copy;
    }
}
