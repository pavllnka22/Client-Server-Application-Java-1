package package1;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StoreServiceTest {

    private StoreServices SUT;

    @BeforeEach
    void create() {
        SUT = new StoreServices();

        SUT.create(new Product("1", "Cheese", "Dairy", 15, new BigDecimal("55.80")));
        SUT.create(new Product("2", "Chocolate", "Sweet", 88, new BigDecimal("67.00")));
        SUT.create(new Product("3", "Cookies", "Sweet", 22, new BigDecimal("82.00")));
        SUT.create(new Product("4", "Bread", "Bakery", 55, new BigDecimal("18.00")));
        SUT.create(new Product("5", "Lavash", "Bakery", 18, new BigDecimal("22.50")));
    }

    @Test
    void shouldDoCRUDOperations() {

        Product milk = new Product("7", "Milk", "Dairy", 33, new BigDecimal("75.00"));
        SUT.create(milk);

        Optional<Product> readProduct = SUT.read("7");
        assertThat(readProduct).isPresent();
        assertThat(readProduct.get().getName()).isEqualTo("Milk");

        milk.setQuantity(10);
        boolean isUpdated = SUT.update(milk);
        assertThat(isUpdated).isTrue();
        assertThat(SUT.read("7").get().getQuantity()).isEqualTo(10);

        boolean isDeleted = SUT.delete("7");
        assertThat(isDeleted).isTrue();
        assertThat(SUT.read("7")).isEmpty();
    }

    @Test
    void shouldFilterDynamicallyByNameAndCategory() {
        ProductFilter filter = new ProductFilter();
        filter.name = "Chocolate";
        filter.category = "Sweet";

        List<Product> result = SUT.search(filter, 1, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Chocolate");
    }

    @Test
    void shouldFilterByPriceRange() {
        ProductFilter filter = new ProductFilter();

        filter.minPrice = new BigDecimal("18.00");
        filter.maxPrice = new BigDecimal("50.00");

        List<Product> result = SUT.search(filter, 1, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("Lavash", "Bread");
    }

    @Test
    void shouldShowPagination() {
        ProductFilter filter = new ProductFilter();
        filter.category = "Dairy";

        List<Product> page1 = SUT.search(filter, 1, 2);
        assertThat(page1).hasSize(1);


        List<Product> page2 = SUT.search(filter, 2, 2);
        assertThat(page2).hasSize(0);
    }

    @Test
    void shouldReturnAnEmptyListIfNoMatches() {
        ProductFilter filter = new ProductFilter();
        filter.minQuantity = 777;

        List<Product> result = SUT.search(filter, 1, 10);
        assertThat(result).isEmpty();
    }
}
