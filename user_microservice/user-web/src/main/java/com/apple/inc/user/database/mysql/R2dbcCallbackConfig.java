package com.apple.inc.user.database.mysql;

import com.apple.inc.user.entities.r2dbc.AuditableEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import reactor.core.publisher.Mono;

import static com.apple.inc.user.util.date.DateUtils.now;

@Configuration
public class R2dbcCallbackConfig {

    @Bean
    public BeforeConvertCallback<AuditableEntity> auditableEntityCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.getCreatedDate() == null) {
                entity.setCreatedDate(now()); // Acts like @PrePersist
            }
            entity.setUpdatedDate(now());     // Acts like @PreUpdate
            return Mono.just(entity);
        };
    }
}
