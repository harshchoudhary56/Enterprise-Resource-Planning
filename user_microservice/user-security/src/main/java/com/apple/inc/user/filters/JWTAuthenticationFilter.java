//package com.apple.inc.user.filters;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.apple.inc.user.dto.LoginRequest;
//import com.apple.inc.user.util.jwt.JWTUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@RequiredArgsConstructor
//public class JWTAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JWTUtil jwtUtil;
//    private final AuthenticationManager authenticationManager;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        if (!request.getServletPath().equals("/user/auth/generate-token")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
//
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
//        Authentication authResult = authenticationManager.authenticate(authToken);
//
//        if (authResult.isAuthenticated()) {
//            String token = jwtUtil.generateToken(authResult.getName(), 15); //15min
//            response.setHeader("Authorization", "Bearer " + token);
//
//            String refreshToken = jwtUtil.generateToken(authResult.getName(), 7 * 24 * 60); //7day
//
//            // Set Refresh Token in HttpOnly Cookie
//            //we can also send it in response body but then client has to store it in local storage or in-memory
//            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
//            refreshCookie.setHttpOnly(true); //prevent javascript from accessing it
//            refreshCookie.setSecure(true); // sent only over HTTPS
//            refreshCookie.setPath("/user/auth/refresh-token"); // Cookie available only for refresh endpoint
//            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days expiry
//            response.addCookie(refreshCookie);
//        }
//    }
//}
