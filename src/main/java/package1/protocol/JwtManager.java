package package1.protocol;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;

public class JwtManager {
    private static final String SECRET = "SuperSecretKey67";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    private static final String ISSUER = "StoreServerHTTP";

    public static String createToken(String username) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1800000))
                .sign(ALGORITHM);
    }

    public static String verifyToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(ALGORITHM)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
            return jwt.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
