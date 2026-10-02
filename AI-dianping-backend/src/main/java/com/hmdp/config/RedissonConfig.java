package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RedissonConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.data.redis")
    public RedissonProperties redissonProperties() {
        return new RedissonProperties();
    }

    @Bean
    public RedissonClient redissonClient(RedissonProperties properties) {
        Config config = new Config();
        String address = "redis://" + properties.getHost() + ":" + properties.getPort();
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(properties.getDatabase())
                .setPassword(StringUtils.hasText(properties.getPassword()) ? properties.getPassword() : null);
        return Redisson.create(config);
    }

    public static class RedissonProperties {
        /** Redis 主机名 */
        private String host = "localhost";
        /** Redis 端口 */
        private int port = 6379;
        /** Redis 密码，可为空 */
        private String password;
        /** Redis 库 */
        private int database = 0;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }
    }
}
