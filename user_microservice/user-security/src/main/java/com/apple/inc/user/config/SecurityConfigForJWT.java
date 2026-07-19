//package com.apple.inc.user.config.jwt;
//
//import com.apple.inc.user.filters.JWTAuthenticationFilter;
//import com.apple.inc.user.filters.JWTRefreshFilter;
//import com.apple.inc.user.filters.JWTValidationFilter;
//import com.apple.inc.user.util.jwt.JWTUtil;
//import com.apple.inc.user.authenticationProviders.JWTAuthenticationProvider;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.ProviderManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import java.util.Arrays;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfigForJWT {
//
//    private final JWTUtil jwtUtil;
//    private final UserDetailsService userDetailsService;
//
//    @Bean
//    public JWTAuthenticationProvider jwtAuthenticationProvider() {
//        return new JWTAuthenticationProvider(jwtUtil, userDetailsService);
//    }
//
//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager,
//                                                   JWTUtil jwtUtil) throws Exception {
//
//        // Authentication filter responsible for login
//        JWTAuthenticationFilter jwtAuthFilter = new JWTAuthenticationFilter(jwtUtil, authenticationManager);
//
//        // Validation filter for checking JWT in every request
//        JWTValidationFilter jwtValidationFilter = new JWTValidationFilter(authenticationManager);
//
//        // refresh filter for checking JWT in every request
//        JWTRefreshFilter jwtRefreshFilter = new JWTRefreshFilter(jwtUtil, authenticationManager);
//
//
//        http.authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/user/auth/generate-token", "/user/auth/refresh-token", "/user/auth/register").permitAll()
//                        .anyRequest().authenticated())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .csrf(AbstractHttpConfigurer::disable)
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // generate token filter
//                .addFilterAfter(jwtRefreshFilter, JWTAuthenticationFilter.class) // refresh token filter
//                .addFilterAfter(jwtValidationFilter, JWTRefreshFilter.class); // validation filter
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager() {
//        return new ProviderManager(Arrays.asList(
//                daoAuthenticationProvider(),
//                jwtAuthenticationProvider()
//        ));
//    }
//}