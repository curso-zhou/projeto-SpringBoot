package br.pucpr.projeto.core.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    // Em produção, externalizar em config e usar 256 bits reais
    private final SecretKey key = Keys.hmacShaKeyFor("trocar-esta-chave-super-secreta-para-uma-de-256-bits-123456".getBytes());
    private final long expirationSeconds = 3600; // 1h

    public String generate(Long id, String email, Set<String> roles) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(email)
        .claim("uid", id)
        .claim("roles", String.join(",", roles))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
    }

    public JwtUserData parse(String token) {
    var claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        String rolesStr = claims.get("roles", String.class);
        Set<String> roles = rolesStr == null || rolesStr.isBlank() ? Set.of() :
                Arrays.stream(rolesStr.split(",")).collect(Collectors.toSet());
        return new JwtUserData(
                claims.get("uid", Long.class),
        claims.getSubject(),
                roles
        );
    }

    public record JwtUserData(Long id, String email, Set<String> roles) {}
}
