//package com.apple.inc.user.token;
//
//import lombok.Getter;
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//
//@Getter
//public class JWTAuthenticationToken extends AbstractAuthenticationToken {
//
//    private final String token;
//
//    public JWTAuthenticationToken(String token) {
//        super(null);
//        this.token = token;
//        setAuthenticated(false);
//    }
//
//    @Override
//    public Object getCredentials() {
//        return token;
//    }
//
//    @Override
//    public Object getPrincipal() {
//        return null;
//    }
//}
