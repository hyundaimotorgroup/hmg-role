package com.hmg.role.common.config;

import java.util.Map;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.hibernate.RedissonRegionFactory;
import org.springframework.util.StringUtils;

public class RedissonConfig extends RedissonRegionFactory {
    public static final String REDISSON_CONFIG_PREFIX = "hibernate.cache.redisson.";
    public static final String REDISSON_ADDRESS_KEY = "address";
    public static final String REDISSON_USERNAME_KEY = "username";
    public static final String REDISSON_PASSWORD_KEY = "password";
    public static final String REDISSON_CLIENT_NAME_KEY = "clientName";
    public static final String REDISSON_TIMEOUT_KEY = "timeout";
    public static final String REDISSON_CONNECT_TIMEOUT_KEY = "connectTimeout";
    public static final String REDISSON_IS_CLUSTER_SERVER_ENABLED_KEY = "isClusterServerEnabled";
    public static final String REDISSON_SCAN_INTERVAL_KEY = "scanInterval";

    @Override
    protected RedissonClient createRedissonClient(
            StandardServiceRegistry registry, Map properties) {

        var address =
                ConfigurationHelper.getString(
                        REDISSON_CONFIG_PREFIX + REDISSON_ADDRESS_KEY, properties);
        var username =
                ConfigurationHelper.getString(
                        REDISSON_CONFIG_PREFIX + REDISSON_USERNAME_KEY, properties);
        var password =
                ConfigurationHelper.getString(
                        REDISSON_CONFIG_PREFIX + REDISSON_PASSWORD_KEY, properties);
        var clientName =
                ConfigurationHelper.getString(
                        REDISSON_CONFIG_PREFIX + REDISSON_CLIENT_NAME_KEY, properties);
        var timeout =
                ConfigurationHelper.getInteger(
                        REDISSON_CONFIG_PREFIX + REDISSON_TIMEOUT_KEY, properties);
        var connectTimeout =
                ConfigurationHelper.getInteger(
                        REDISSON_CONFIG_PREFIX + REDISSON_CONNECT_TIMEOUT_KEY, properties);

        var isClusterServerEnabled =
                ConfigurationHelper.getBoolean(
                        REDISSON_CONFIG_PREFIX + REDISSON_IS_CLUSTER_SERVER_ENABLED_KEY,
                        properties);

        int masterSlaveScanIntervalMs =
                ConfigurationHelper.getInteger(
                        REDISSON_CONFIG_PREFIX + REDISSON_SCAN_INTERVAL_KEY, properties);

        Config config = new Config();

        var server =
                isClusterServerEnabled
                        ? config.useClusterServers()
                                .addNodeAddress(address)
                                .setReadMode(ReadMode.MASTER_SLAVE)
                                .setScanInterval(masterSlaveScanIntervalMs)
                        : config.useSingleServer().setAddress(address);

        server.setClientName(clientName).setTimeout(timeout).setConnectTimeout(connectTimeout);

        if (StringUtils.hasText(username)) server.setUsername(username);
        if (StringUtils.hasText(password)) server.setPassword(password);

        RedissonClient client = Redisson.create(config);
        client.getKeys().flushall();
        return client;
    }
}
