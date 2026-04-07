package com.ngtoan.phone_store.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "mysecretkeymysecretkeymysecretkey"; // >= 32 chars
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 🔹 tạo token
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 ngày
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔹 lấy username từ token
    public static String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 🔹 kiểm tra token hợp lệ
    public static boolean validateToken(String token, String username) {

        final String extractedUsername = extractUsername(token);

        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    // 🔹 kiểm tra token hết hạn
    private static boolean isTokenExpired(String token) {

        Date expiration = extractAllClaims(token).getExpiration();

        return expiration.before(new Date());
    }

    // 🔹 lấy tất cả claims
    private static Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}