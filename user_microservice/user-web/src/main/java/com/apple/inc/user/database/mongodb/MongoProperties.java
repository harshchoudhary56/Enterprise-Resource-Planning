package com.apple.inc.user.database.mongodb;

import com.apple.inc.user.constants.DatabaseConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = DatabaseConstants.MONGODB)
public class MongoProperties {

    private String uri;
    private String database;
}
