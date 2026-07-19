package com.apple.inc.gateway.config.mongodb;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@RefreshScope
@ConfigurationProperties(prefix = "spring.data.mongodb")
public class MongoProperties {

    private String uri;

    private String database;
}