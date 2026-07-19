//package com.apple.inc.user.authenticationProviders;
//
//import com.apple.inc.user.token.JWTAuthenticationToken;
//import com.apple.inc.user.util.jwt.JWTUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class JWTAuthenticationProvider implements AuthenticationProvider {
//
//    private final JWTUtil jwtUtil;
//    private final UserDetailsService userDetailsService;
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        String token = ((JWTAuthenticationToken) authentication).getToken();
//
//        String username = jwtUtil.validateAndExtractUsername(token);
//        if (username == null) {
//            throw new BadCredentialsException("Invalid JWT Token");
//        }
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//    }
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return JWTAuthenticationToken.class.isAssignableFrom(authentication);
//    }
//}
