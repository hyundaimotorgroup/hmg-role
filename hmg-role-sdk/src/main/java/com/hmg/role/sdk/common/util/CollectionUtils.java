package com.hmg.role.sdk.common.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionUtils {

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> c) {
        return c != null && !c.isEmpty();
    }

    /**
     * Returns the set difference between the left and right sets.
     *
     * <p>This method computes the relative complement of the right set in the left set, meaning it
     * returns all elements that are present in the {@code left} set but not in the {@code right}
     * set.
     *
     * @param left the set from which elements will be retained
     * @param right the set containing elements to exclude from the left set
     * @return a new set containing elements in {@code left} that are not in {@code right}
     * @param <T> Type of the collections. {@code left} and {@code right} should have the same type
     *     parameter
     */
    public static <T> Set<T> getLeftDifference(Collection<T> left, Collection<T> right) {
        Set<T> result = new HashSet<>();
        for (T s : left) {
            if (!right.contains(s)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Returns the intersection of two sets as a new set.
     *
     * <p>This method does not modify the input sets. The result contains only the elements that are
     * present in both {@code set1} and {@code set2}.
     *
     * @param <T> the type of elements in the sets
     * @param left the first set
     * @param right the second set
     * @return a new set containing the common elements of {@code set1} and {@code set2}
     * @throws NullPointerException if either {@code set1} or {@code set2} is {@code null}
     */
    public static <T> Set<T> getSetIntersection(Set<T> left, Set<T> right) {
        return left.stream().filter(right::contains).collect(Collectors.toSet());
    }
}
