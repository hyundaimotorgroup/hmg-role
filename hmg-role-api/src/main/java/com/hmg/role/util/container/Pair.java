package com.hmg.role.util.container;

/**
 * A class to hold a pair (an (A, B) tuple) of objects.
 *
 * @param first The first (left side) object of the tuple.
 * @param second The second (right side) object of the tuple.
 * @param <A> The first object's class.
 * @param <B> The second object's class.
 */
public record Pair<A, B>(A first, B second) {
    // It's questionable why there's nothing like this in java.lang
}
