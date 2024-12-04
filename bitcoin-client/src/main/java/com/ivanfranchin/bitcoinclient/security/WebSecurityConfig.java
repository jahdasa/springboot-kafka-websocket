package com.ivanfranchin.bitcoinclient.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final String USER = "USER";

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user1 = User.withUsername("user1")
                .password(passwordEncoder().encode("123"))
                .roles(USER).build();

        UserDetails user2 = User.withUsername("user2")
                .password(passwordEncoder().encode("123"))
                .roles(USER).build();

        return new InMemoryUserDetailsManager(user1, user2);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection
                .anonymous(anonymousConfigurer -> anonymousConfigurer
                        .principal(createAnonymousUser()) // Set the anonymous user
                )
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/streamer").permitAll() // Allow unauthenticated access to /streamer
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Define the specific user for anonymous users
    private Object createAnonymousUser() {
        return new AnonymousAuthenticationToken(
                "user1",
                "123", // The username for the anonymous user
                Collections.singletonList(new SimpleGrantedAuthority("USER")) // Authorities
        );
    }
}
