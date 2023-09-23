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

//    @Autowired
//    UserRepository userRepository;

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
//                return onError(exchange, "no authorizition header", HttpStatus.UNAUTHORIZED);
//                ServerHttpResponse response = exchange.getResponse();
//                response.setStatusCode(HttpStatus.UNAUTHORIZED);
//                response.getHeaders().add("Content-Type", "text/plain");
//                response.getHeaders().add("Cache-Control", "no-store");
//                return response.writeWith(Mono.just(response.bufferFactory().wrap("No Unauthorized".getBytes())));

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
//                ServerHttpResponse response = exchange.getResponse();
//                response.setStatusCode(HttpStatus.UNAUTHORIZED);
//                response.getHeaders().add("Content-Type", "text/plain");
//                response.getHeaders().add("Cache-Control", "no-store");
//                return response.writeWith(Mono.just(response.bufferFactory().wrap("Unauthorized message".getBytes())));
                // Set unauthorized status and write a custom JSON response
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
        Long userCode = null;
        boolean returnValue = true;
        String nickname = null;
        try {
            userCode = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwt).getClaim("id").asLong();
            nickname = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwt).getClaim("nickname").asString();
            System.out.println(userCode);
            System.out.println(nickname);
            returnValue = true;
        }catch (Exception e){
            returnValue = false;
        }
//        String subject = null;
//        Long userCode = null;
//        try {
//            subject = Jwts.parserBuilder().setSigningKey(env.getProperty("token.secret")).build()
//                    .parseClaimsJws(jwt).getBody()
//                    .getSubject();
//            userCode = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwt).getClaim("id").asLong();
//            System.out.println(userCode);
//        } catch (Exception exception) {
//            returnValue = false;
//        }
//        if (subject == null || subject.isEmpty()) {
//            returnValue = false;
//        }

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
