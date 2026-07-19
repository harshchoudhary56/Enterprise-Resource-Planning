package com.apple.inc.gateway.config.routes;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/user/auth/generate-token",
            "/user/auth/refresh-token",
            "/user/auth/register"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
