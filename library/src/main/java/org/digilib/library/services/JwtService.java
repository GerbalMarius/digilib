package org.digilib.library.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class JwtService {

    private final Key key;
    private final long accessTlsSeconds;
    private final long refreshTlsSeconds;


    public JwtService(
            @Value("${security.jwt.secret-key}") String secret,
            @Value("${security.jwt.token-expiration-minutes}") long accessMinutes,
            @Value("${security.jwt.refresh-token-expiration-days}") long refreshDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTlsSeconds = accessMinutes * 60;
        this.refreshTlsSeconds = refreshDays * 24 * 60 * 60;
    }

    public String generateAccessToken(UserDetails claimsPrincipal) {
        var now = Instant.now();

        Map<String, Object> claims = HashMap.newHashMap(1);
        List<String> authoritiesList = claimsPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        claims.put("roles", authoritiesList);

        return Jwts.builder()
                .setSubject(claimsPrincipal.getUsername())
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTlsSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmailFromToken(String token) {
        Jws<Claims> claimsJws = parseTokenClaims(token);
        return claimsJws.getBody().getSubject();
    }

    public List<String> extractRolesFromToken(String token) {
        Claims claimsJws = parseTokenClaims(token).getBody();
        Object roles = claimsJws.get("roles");

        if (roles instanceof Collection<?> coll) {
            return coll.stream()
                    .map(Objects::toString)
                    .toList();
        } else {
            log.error("Roles claim is not a collection");
            return List.of();
        }
    }

    public boolean isValidToken(String token, UserDetails claimsPrincipal) {
       try{
           Claims claimsJws = parseTokenClaims(token).getBody();
           String subject = claimsJws.getSubject();

           return subject.equals(claimsPrincipal.getUsername()) && !isExpired(token);
       } catch (Exception e) {
           log.debug("Error parsing token", e);
           return false;
       }
    }

    public boolean isExpired(String token) {
        Claims expiration = parseTokenClaims(token).getBody();
        Date expirationDate = expiration.getExpiration();
        Date now = new Date();

        return expirationDate.before(now);
    }

    public String generateRefreshToken(String username) {
        var now = Instant.now();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTlsSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Jws<Claims> parseTokenClaims(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();

        return parser.parseClaimsJws(token);
    }
}
