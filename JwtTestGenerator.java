import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public class JwtTestGenerator {
    public static void main(String[] args) {
        // Use the EXACT same secret as application.yml
        String secret = "mySecretKey1234567890123456789012345678901234567890";
        
        System.out.println("🔧 Java JWT Test Generator");
        System.out.println("━".repeat(80));
        System.out.println("🔑 Secret: '" + secret + "'");
        System.out.println("📏 Secret Length: " + secret.length());
        
        // Create secret key EXACTLY as JwtService does
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        System.out.println("🔐 Secret Key Algorithm: " + secretKey.getAlgorithm());
        
        // Create JWT token EXACTLY as the application would expect
        long now = System.currentTimeMillis() / 1000;
        String token = Jwts.builder()
            .subject("test-user")
            .claim("roles", List.of("ADMIN"))
            .issuedAt(new Date(now * 1000))
            .expiration(new Date((now + 3600) * 1000))
            .signWith(secretKey)
            .compact();
        
        System.out.println("✅ Generated JWT Token:");
        System.out.println("━".repeat(80));
        System.out.println(token);
        System.out.println("━".repeat(80));
        System.out.println("📏 Token Length: " + token.length());
        
        System.out.println("\n🧪 Test Command:");
        System.out.println("curl -s -w \"%{http_code}\" -H \"Authorization: Bearer " + token + "\" -H \"Content-Type: application/json\" \"http://localhost:8080/api/v1/exceptions\"");
        
        // Validate the token using the same logic as JwtService
        try {
            var claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            System.out.println("\n✅ Token validation successful!");
            System.out.println("👤 Subject: " + claims.getSubject());
            System.out.println("🛡️  Roles: " + claims.get("roles"));
            System.out.println("⏰ Expires: " + claims.getExpiration());
        } catch (Exception e) {
            System.out.println("\n❌ Token validation failed: " + e.getMessage());
        }
    }
}