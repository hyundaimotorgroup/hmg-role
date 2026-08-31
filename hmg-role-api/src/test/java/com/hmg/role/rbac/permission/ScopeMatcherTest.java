package com.hmg.role.rbac.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScopeMatcherTest {

    @Autowired private ScopeMatcher scopeMatcher;

    @Test
    void testIsScopeAllowed_WithSinglePolicyAndRequestScope_isTrue() {
        boolean expectedResult = true;
        var policyScope = "com";
        var requestScope = "com";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithSinglePolicyScopeAndHierarchicalRequestScope_isTrue() {
        boolean expectedResult = true;
        var policyScope = "com";
        var requestScope = "com.test";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithSinglePolicyScopeAndWrongSingleRequestScope_isFalse() {
        boolean expectedResult = false;
        var policyScope = "com";
        var requestScope = "cam";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithHierarchicalPolicyScopeAndHierarchicalRequestScope_isTrue() {
        boolean expectedResult = true;
        var policyScope = "com.test";
        var requestScope = "com.test";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithHierarchicalPolicyScopeAndHierarchicalRequestScope_2_isTrue() {
        boolean expectedResult = true;
        var policyScope = "com.test";
        var requestScope = "com.test.fast";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithHierarchicalPolicyScopeAndSingleRequestScope_isFalse() {
        boolean expectedResult = false;
        var policyScope = "com.test";
        var requestScope = "com";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithHierarchicalPolicyScopeAndWrongRequestScope_isFalse() {
        boolean expectedResult = false;
        var policyScope = "com.test";
        var requestScope = "cam";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithMultiplePolicyScopeAndWrongRequestScope_2_isFalse() {
        boolean expectedResult = false;
        var policyScope = "com.test";
        var requestScope = "cam.test";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithMultiplePolicyScopeAndWrongRequestScope_3_isFalse() {
        boolean expectedResult = false;
        var policyScope = "com.test";
        var requestScope = "com.app";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }

    @Test
    void testIsScopeAllowed_WithMultiplePolicyScopeAndWrongRequestScope_4_isFalse() {
        boolean expectedResult = false;
        var policyScope = "com.test";
        var requestScope = "com.app.test";
        var result = scopeMatcher.isScopeAllowed(policyScope, requestScope);
        assertEquals(expectedResult, result);
    }
}
