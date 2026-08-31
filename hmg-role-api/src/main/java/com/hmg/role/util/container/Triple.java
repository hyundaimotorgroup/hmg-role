package com.hmg.role.util.container;

public record Triple<A, B, C>(A a, B b, C c) {
    // This is a reason why Scala is a thing :(
}
