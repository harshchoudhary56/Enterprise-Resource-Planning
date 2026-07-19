//package com.apple.inc.user.config.asyncconfig;
//
//import com.apple.inc.user.config.properties.ExecutorPoolProperties;
//import com.apple.inc.user.constants.BeanConstants;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//@Configuration
//@RequiredArgsConstructor
//public class ThreadPoolConfiguration {
//
//    private final ExecutorPoolProperties properties;
//
//    @Bean(BeanConstants.API_RECORD_POOL)
//    public ThreadPoolTaskExecutor apiRecordThreadPoolTaskExecutor() {
//        return configuredThreadPoolTaskExecutor(BeanConstants.API_RECORD_POOL,
//                properties.getDefaultCorePoolSize(),
//                properties.getDefaultMaxPoolSize(),
//                properties.getDefaultQueueSize());
//    }
//
//    private ThreadPoolTaskExecutor configuredThreadPoolTaskExecutor(String poolId, int corePoolSize, int maxPoolSize, int queueCapacity) {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setThreadNamePrefix("executor-pool-" + poolId + "-");
//        executor.setCorePoolSize(corePoolSize);
//        executor.setMaxPoolSize(maxPoolSize);
//        executor.setQueueCapacity(queueCapacity);
//        executor.initialize();
//        return executor;
//    }
//}
