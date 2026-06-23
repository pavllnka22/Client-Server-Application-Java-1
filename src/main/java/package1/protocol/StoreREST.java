package package1.protocol;



import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import package1.Product;
import package1.ProductFilter;
import package1.StoreServices;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoreREST {
    private final int port;
    private final StoreServices storeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StoreREST(int port, StoreServices storeService) {
        this.port = port;
        this.storeService = storeService;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/login", this::handleLogin);

        HttpContext productsContext = server.createContext("/products", this::handleProducts);
        productsContext.setAuthenticator(new BearerAuthenticator());

        server.setExecutor(null);
        server.start();

    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }

        try {
            Map<?, ?> credentials = objectMapper.readValue(exchange.getRequestBody(), Map.class);
            String username = (String) credentials.get("login");
            String password = (String) credentials.get("password");


            if ("admin".equals(username) && "admin".equals(password)) {
                String token = JwtManager.createToken(username);
                Map<String, String> response = Map.of("token", token);
                sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
            } else {
                sendResponse(exchange, 403, "Invalid credentials");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad request");
        }
    }

    private void handleProducts(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        try {

            if ("GET".equalsIgnoreCase(method) && pathParts.length == 3) {
                String id = pathParts[2];
                Optional<Product> product = storeService.read(id);
                if (product.isPresent()) {
                    sendResponse(exchange, 200, objectMapper.writeValueAsString(product.get()));
                } else {
                    sendResponse(exchange, 404, "Product was not found");
                }
                return;
            }


            if ("PUT".equalsIgnoreCase(method) && pathParts.length == 2) {
                Product product = objectMapper.readValue(exchange.getRequestBody(), Product.class);


                ProductFilter productFilter = new ProductFilter();
                productFilter.name = product.getName();
                List<Product> existing = storeService.search(productFilter, 1, 1);

                if (!existing.isEmpty() && existing.stream().anyMatch(p -> p.getName().equalsIgnoreCase(product.getName()))) {
                    sendResponse(exchange, 409, "Product names must be unique");
                    return;
                }

                storeService.create(product);
                sendResponse(exchange, 201, objectMapper.writeValueAsString(product));
                return;
            }



            if ("POST".equalsIgnoreCase(method) && pathParts.length == 3) {
                String id = pathParts[2];
                Product product = objectMapper.readValue(exchange.getRequestBody(), Product.class);
                product.setId(id);

                boolean updated = storeService.update(product);
                if (updated) {
                    sendResponse(exchange, 200, "Updated successfully");
                } else {
                    sendResponse(exchange, 404, "Product was not found");
                }
                return;
            }


            if ("DELETE".equalsIgnoreCase(method) && pathParts.length == 3) {
                String id = pathParts[2];
                boolean deleted = storeService.delete(id);
                if (deleted) {
                    sendResponse(exchange, 200, "Deleted successfully");
                } else {
                    sendResponse(exchange, 404, "Product was not found");
                }
                return;
            }

            sendResponse(exchange, 400, "Unsupported request");
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}