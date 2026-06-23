package package1.protocol;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class BearerAuthenticator extends Authenticator {
    @Override
    public Result authenticate(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Failure(401);
        }

        String token = authHeader.substring(7);
        String username = JwtManager.verifyToken(token);

        if (username == null) {
            return new Failure(401);
        }

        return new Success(new HttpPrincipal(username, "realm"));
    }
}