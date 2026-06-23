package package1;



import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import package1.protocol.StoreREST;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StoreServerHTTPTest {

    private static final int PORT = 8085;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static final StoreServices storeService = new StoreServices();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String jwtToken;

    @BeforeAll
    static void startServer() throws Exception {
        StoreREST server = new StoreREST(PORT, storeService);
        server.start();

        storeService.create(new Product("100", "Milk", "Dairy", 10, new BigDecimal("65.00")));

        Map<String, String> credentials = Map.of("login", "admin", "password", "admin");
        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(credentials)))
                .build();

        HttpResponse<String> response = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        Map<?, ?> responseMap = mapper.readValue(response.body(), Map.class);
        jwtToken = (String) responseMap.get("token");
    }

    @Test
    void shouldBlockUnauthenticatedRequests() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products/100"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldGetProductById() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products/100"))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        Product product = mapper.readValue(response.body(), Product.class);
        assertThat(product.getName()).isEqualTo("Milk");
    }

    @Test
    void shouldPutNewProductAndBlockDuplicateName() throws Exception {
        Product newProduct = new Product("107", "Cocoa", "Sweets", 67, new BigDecimal("92.00"));


        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products"))
                .header("Authorization", "Bearer " + jwtToken)
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(newProduct)))
                .build();

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertThat(response1.statusCode()).isEqualTo(201);


        HttpResponse<String> response2 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertThat(response2.statusCode()).isEqualTo(409);
    }

    @Test
    void shouldUpdateProductViaPost() throws Exception {
        Product updateData = new Product("100", "Milky Milk", "Dairy", 88, new BigDecimal("69.00"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products/100"))
                .header("Authorization", "Bearer " + jwtToken)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(updateData)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(storeService.read("100").get().getName()).isEqualTo("Milky Milk");
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products/100"))
                .header("Authorization", "Bearer " + jwtToken)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(storeService.read("100")).isEmpty();
    }
}
