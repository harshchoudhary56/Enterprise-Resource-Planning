//package com.apple.inc.user.util.jwt;
//
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.util.Date;
//
//public class JWTUtil {
//
//    private static final String SECRET_KEY = "your-secure-secret-key-min-32bytes";
//    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
//
//    public String generateToken(String username, long expiryMinutes) {
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiryMinutes * 60 * 1000)) //in milliseconds
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public String validateAndExtractUsername(String token) {
//        try {
//            return Jwts.parser()
//                    .setSigningKey(key)
//                    .parseClaimsJws(token)
//                    .getBody()
//                    .getSubject();
//        } catch (JwtException e) {
//            return null; // Invalid or expired JWT
//        }
//    }
//}
//
//
