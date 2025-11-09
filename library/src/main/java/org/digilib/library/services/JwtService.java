package org.digilib.library.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.digilib.library.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

        Map<String, Object> claims = HashMap.newHashMap(2);
        List<String> authoritiesList = claimsPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        claims.put("roles", authoritiesList);
        claims.put("id", ((User)claimsPrincipal).getId());

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(claimsPrincipal.getUsername())
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTlsSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T extractFromToken(String token, Function<Claims, T> claimsResolver) {
        Jws<Claims> claimsJws = parseTokenClaims(token);
        Claims body = claimsJws.getBody();
        return claimsResolver.apply(body);
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

    public boolean isRefreshValid(String refreshToken) {
        try {
            parseTokenClaims(refreshToken);
            return true;
        } catch (Exception e) {
            log.debug("Invalid refresh token", e);
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
                .setHeaderParam("typ", "JWT")
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
