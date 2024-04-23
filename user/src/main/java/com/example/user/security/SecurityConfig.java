package com.example.user.security;

import com.example.user.security.jwt.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String FRONT_URL = "http://localhost:3000";
    private final CorsFilter corsFilter;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChain
     * <p>
     * WebSecurityConfigurerAdapter 가 deprecated 되면서 SecurityFilterChain 을 Bean 으로 등록하여 사용
     * </p>
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 예외
     */
    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // http 시큐리티 빌더
        return http
                .httpBasic() // JWT token을 사용하므로 basic 인증 disable
                .disable()
                .csrf() // csrf 비활성화
                .disable()
                .sessionManagement()  // session 을 사용하지 않음
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                .requestMatchers(FRONT_URL + "/main/**").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .exceptionHandling() // 예외 처리
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                .formLogin() // form login disable
                .disable()
                .addFilter(corsFilter) // @CrossOrigin(인증X), 시큐리티 필터에 등록 인증(O)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class) // JWT 필터 등록
                .build();
    }
}
