package com.apple.inc.gateway;

import com.apple.inc.gateway.loadbalancer.LoadBalancerAcademicsClientConfig;
import com.apple.inc.gateway.loadbalancer.LoadBalancerUserClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@EnableDiscoveryClient
@SpringBootApplication
@RemoteApplicationEventScan(basePackages = "com.apple.inc.gateway.config")
@LoadBalancerClients(value = {
        @LoadBalancerClient(name = "user-service", configuration = LoadBalancerUserClientConfig.class),
        @LoadBalancerClient(name = "academics-service", configuration = LoadBalancerAcademicsClientConfig.class)
})
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
