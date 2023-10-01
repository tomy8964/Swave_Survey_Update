package com.example.apigatewayservice.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.apigatewayservice.OAuth.JwtProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    Environment env;
    private final ObjectMapper objectMapper;

    public AuthorizationHeaderFilter(Environment env, ObjectMapper objectMapper) {
        super(Config.class);
        this.env = env;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                response.getHeaders().add("Cache-Control", "no-store");

                ErrorResponse errorResponse = new ErrorResponse("No authorized");
                try {
                    byte[] errorResponseBytes = objectMapper.writeValueAsBytes(errorResponse);
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(errorResponseBytes)));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }

            String authorizationHeader = request.getHeaders().get(AUTHORIZATION).get(0);
            System.out.println(authorizationHeader);
            String jwt = authorizationHeader.replace("Bearer ", "");
            System.out.println(jwt);

            if (!isJwtValid(jwt)) {
                System.out.println("error");
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                response.getHeaders().add("Cache-Control", "no-store");

                ErrorResponse errorResponse = new ErrorResponse("Unauthorized message");
                try {
                    byte[] errorResponseBytes = objectMapper.writeValueAsBytes(errorResponse);
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(errorResponseBytes)));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }
            return chain.filter(exchange);
        };
    }

    private boolean isJwtValid(String jwt) {
        boolean returnValue;
        try {
            JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwt).getClaim("id").asLong();
            JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwt).getClaim("nickname").asString();
            returnValue = true;
        }catch (Exception e){
            returnValue = false;
        }

        return returnValue;
    }

    // Mono, Flux -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(err);
        return response.setComplete();
    }

    public static class Config{

    }

    public static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
