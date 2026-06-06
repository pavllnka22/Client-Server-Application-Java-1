package package1;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoreServices {

    private final Map<String, Product> database = new ConcurrentHashMap<>();


    public void create(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }
        database.put(product.getId(), product);
    }

    public Optional<Product> read(String id) {
        return Optional.ofNullable(database.get(id));
    }


    public boolean update(Product product) {
        if (database.containsKey(product.getId())) {
            database.put(product.getId(), product);
            return true;
        }
        return false;
    }


    public boolean delete(String id) {
        return database.remove(id) != null;
    }

    public List<Product> search(ProductFilter filter, int page, int pageSize) {
        Stream<Product> stream = database.values().stream();

        if (filter.name != null && !filter.name.isEmpty()) {
            stream = stream.filter(p -> p.getName().toLowerCase().contains(filter.name.toLowerCase()));
        }
        if (filter.category != null && !filter.category.isEmpty()) {
            stream = stream.filter(p -> p.getCategory().equalsIgnoreCase(filter.category));
        }
        if (filter.minQuantity != null) {
            stream = stream.filter(p -> p.getQuantity() >= filter.minQuantity);
        }
        if (filter.maxQuantity != null) {
            stream = stream.filter(p -> p.getQuantity() <= filter.maxQuantity);
        }
        if (filter.maxPrice != null) {
            stream = stream.filter(p -> p.getPrice().compareTo(filter.maxPrice) <= 0);
        }
        if (filter.minPrice != null) {
            stream = stream.filter(p -> p.getPrice().compareTo(filter.minPrice) >= 0);
        }

        int skipsNum = (page - 1) * pageSize;

        return stream.skip(skipsNum).limit(pageSize).collect(Collectors.toList());
    }
}
