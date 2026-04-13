package com.forsy.conf.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility component for generating, parsing, and validating JSON Web Tokens (JWT).
 *
 * <p>This class handles the cryptographic signing of tokens and extracts claims
 * such as the username, roles, and expiration dates required by the security filter.
 *
 * @author Illia
 */
@Component
public class JwtUtils {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  /**
   * Extracts the username (subject) from the given JWT.
   *
   * @param token the JSON Web Token
   * @return the username extracted from the token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts a specific claim from the JWT using the provided resolver function.
   *
   * @param token          the JSON Web Token
   * @param claimsResolver a function to extract the desired claim from the parsed body
   * @param <T>            the type of the claim to return
   * @return the extracted claim
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
        .parseClaimsJws(token).getBody();
  }

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Generates a new JWT for the specified user.
   *
   * <p>The generated token includes the user's authorities (roles) as custom claims
   * and sets an expiration date based on the configured environment properties.
   *
   * @param userDetails the authenticated user details
   * @return a fully constructed, cryptographically signed JWT string
   */
  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", userDetails.getAuthorities());
    return createToken(claims, userDetails.getUsername());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Validates the provided JWT against the given user details.
   *
   * <p>A token is considered valid if the username extracted from the token matches
   * the provided user's username, and the token has not yet expired.
   *
   * @param token       the JSON Web Token to validate
   * @param userDetails the user details to compare against the token's claims
   * @return {@code true} if the token is valid and belongs to the user, {@code false} otherwise
   */
  public boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Retrieves the JWT from the HTTP request cookies.
   *
   * <p>Scans the request for a cookie named "access_token" and returns its value.
   *
   * @param request the current HTTP request
   * @return the JWT string if the cookie is present, or {@code null} if not found
   */
  public String getTokenFromRequest(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("access_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
