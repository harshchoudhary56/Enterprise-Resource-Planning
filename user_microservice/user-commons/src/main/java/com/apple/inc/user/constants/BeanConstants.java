package com.apple.inc.user.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BeanConstants {

    // ═══════════════ MySQL R2DBC ═══════════════
    public static final String MYSQL_R2DBC_PROPERTIES = "mysqlR2dbcProperties";
    public static final String MYSQL_CONNECTION_FACTORY = "mysqlConnectionFactory";
    public static final String MYSQL_DATABASE_CLIENT = "mysqlDatabaseClient";
    public static final String MYSQL_TRANSACTION_MANAGER = "mysqlTransactionManager";

    // ═══════════════ MongoDB Reactive ═══════════════
    public static final String MONGODB_CLIENT = "reactiveMongoClient";
    public static final String MONGODB_DATASOURCE = "reactiveMongoDatabaseFactory";
    public static final String REACTIVE_MONGO_TEMPLATE = "reactiveMongoTemplate";
    public static final String MONGODB_TRANSACTION_MANAGER = "mongodbTransactionManager";

    // ═══════════════ Thread Pools ═══════════════
    public static final String API_RECORD_POOL = "apiRecordPool";
}
