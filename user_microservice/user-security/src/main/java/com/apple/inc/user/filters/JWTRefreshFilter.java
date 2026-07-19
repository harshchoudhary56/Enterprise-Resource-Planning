//package com.apple.inc.user.filters;
//
//import com.apple.inc.user.token.JWTAuthenticationToken;
//import com.apple.inc.user.util.jwt.JWTUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@RequiredArgsConstructor
//public class JWTRefreshFilter extends OncePerRequestFilter {
//
//    private final JWTUtil jwtUtil;
//    private final AuthenticationManager authenticationManager;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        if (!request.getServletPath().equals("/user/auth/refresh-token")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String refreshToken = extractJwtFromRequest(request);
//        if (refreshToken == null) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return;
//        }
//
//        JWTAuthenticationToken authenticationToken = new JWTAuthenticationToken(refreshToken);
//        Authentication authResult = authenticationManager.authenticate(authenticationToken);
//        if(authResult.isAuthenticated()) {
//            String newToken = jwtUtil.generateToken(authResult.getName(), 15); //15min
//            response.setHeader("Authorization", "Bearer " + newToken);
//        }
//    }
//
//    private String extractJwtFromRequest(HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies == null) {
//            return null;
//        }
//        String refreshToken = null;
//        for (Cookie cookie : cookies) {
//            if ("refreshToken".equals(cookie.getName())) {
//                refreshToken = cookie.getValue();
//            }
//        }
//        return refreshToken;
//    }
//}
