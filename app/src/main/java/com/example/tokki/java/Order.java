package com.example.tokki.java;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private Store store;
    private List<Product> products;
    private List<Integer> quantities;

    public Order(Store store, List<Product> products, List<Integer> quantities) {
        this.store = store;
        this.products = products;
        this.quantities = quantities;
    }

    public Store getStore() { return store; }
    public List<Product> getProducts() { return products; }
    public List<Integer> getQuantities() { return quantities; }

    // Calculate total price
    public double getTotal() {
        double total = 0;
        for (int i = 0; i < products.size(); i++) {
            total += products.get(i).getPrice() * quantities.get(i);
        }
        return total;
    }
}
