package com.hmg.role.common.config.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Deprecated
public class SdkToInstantiateChecker implements Condition {
    private static final String[] NO_HEALTHCHECK_ENVS = new String[] {"local"};

    public SdkToInstantiateChecker() {}

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        boolean isDev = env.acceptsProfiles(Profiles.of(NO_HEALTHCHECK_ENVS));
        return !isDev;
    }
}
