package org.example.springreact.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        var reactiveAuthenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        reactiveAuthenticationManager.setPasswordEncoder(passwordEncoder);
        return reactiveAuthenticationManager;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager) {
        return buildDefaultHttpSecurity(http)
                .authenticationManager(authenticationManager)
                .build();
    }

    private ServerHttpSecurity buildDefaultHttpSecurity(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange((auth) -> auth
//                        .pathMatchers("/api/functions/users/**").hasAnyRole("USER", "MANAGER")
//                        .pathMatchers(HttpMethod.POST, "/api/functions/users").permitAll()
//                        .pathMatchers(HttpMethod.GET, "/api/functions/tasks/**").hasAnyRole("USER", "MANAGER")
//                        .pathMatchers(HttpMethod.POST, "/api/functions/tasks/{id}/addObserver").hasAnyRole("USER", "MANAGER")
//                        .pathMatchers(HttpMethod.POST, "/api/functions/tasks/").hasRole("MANAGER")
//                        .pathMatchers(HttpMethod.PUT, "/api/functions/tasks/*").hasRole("MANAGER")
//                        .pathMatchers(HttpMethod.DELETE, "/api/functions/tasks/*").hasRole("MANAGER")
                        .pathMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .anyExchange().authenticated())
                .httpBasic(Customizer.withDefaults());
    }
}
