package com.example.gateway.filter;

import com.example.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private RouteValidator validator;
    private JwtUtil jwtUtil;

    @Autowired
    public void setRouteValidator(RouteValidator validator) {
        this.validator = validator;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                handleRequest(exchange);
            }
            return chain.filter(exchange);
        });
    }

    private void handleRequest(ServerWebExchange exchange) {
        checkMissingHeader(exchange);
        handleToken(exchange);
    }

    private void handleToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader = authHeader.substring(7);
        }
        try {
            jwtUtil.validateToken(authHeader);

        } catch (Exception e) {
            throw new RuntimeException("invalid token");
        }
    }

    private static void checkMissingHeader(ServerWebExchange exchange) {
        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            throw new RuntimeException("missing authorization header");
        }
    }

    public static class Config {
    }
}
