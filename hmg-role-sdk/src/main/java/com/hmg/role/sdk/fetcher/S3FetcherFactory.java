package com.hmg.role.sdk.fetcher;

import com.hmg.role.sdk.common.SdkConstants;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Singular;

public class S3FetcherFactory implements AutoCloseable {

    private final Map<String, DefaultS3FetcherService> registry = new ConcurrentHashMap<>();

    @Builder
    public S3FetcherFactory(@Singular("profile") Iterable<S3Profile> profiles) {
        for (S3Profile p : profiles) {
            if (registry.putIfAbsent(p.getAlias(), DefaultS3FetcherService.of(p)) != null) {
                throw new IllegalArgumentException("Duplicate S3 profile alias: " + p.getAlias());
            }
        }
    }

    public S3FetcherService get() {
        return get(SdkConstants.DEFAULT_S3_BUCKET_PROFILE);
    }

    public S3FetcherService get(String alias) {
        DefaultS3FetcherService svc = registry.get(alias);
        if (svc == null) throw new IllegalArgumentException("No S3 profile registered: " + alias);

        return svc;
    }

    public Set<String> aliases() {
        return Collections.unmodifiableSet(new HashSet<String>(registry.keySet()));
    }

    @Override
    public void close() {
        for (DefaultS3FetcherService svc : registry.values()) {
            try {
                svc.close();
            } catch (Exception ignored) {
            }
        }
        registry.clear();
    }
}
