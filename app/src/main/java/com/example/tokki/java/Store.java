package com.example.tokki.java;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Store implements Serializable {
    private String storeName, foodCategory, storeLogo;
    private double latitude, longitude, stars;
    private int noOfVotes;
    private String filepath;
    private List<Product> products;
    private String priceCategory;

    public Store(String storeName, double latitude, double longitude,String foodCategory, double stars, int noOfVotes, String storeLogo) {
        this.storeName = storeName;
        this.foodCategory = foodCategory;
        this.storeLogo = storeLogo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
    }

    public Store(@JsonProperty("StoreName") String storeName,
                 @JsonProperty("Latitude") double latitude,
                 @JsonProperty("Longitude") double longitude,
                 @JsonProperty("FoodCategory") String foodCategory,
                 @JsonProperty("Stars") int stars,
                 @JsonProperty("NoOfVotes") int noOfVotes,
                 @JsonProperty("StoreLogo") String storeLogo,
                 @JsonProperty("Products") List<Product> products) {
        /*
        Constructor gia na xrhsimopoihthei sto parsing apo json
         */
        this.storeName = storeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.storeLogo = storeLogo;
        this.products = products;
    }

    public Store() {
    }

    @JsonIgnore
    public String getFilepath(){
        return this.filepath;
    }

    @JsonIgnore
    public String getPriceCategory(){
        return this.priceCategory;
    }

    public void setFilepath(String filepath){
        this.filepath = filepath;
    }

    public void calculatePriceCategory(){
        int counter = 0;
        double total = 0;
        for(Product product : products){
            if(product.isOnline()){
                total += product.getPrice();
                counter++;
            }
        }
        double avg = total / counter;
        if(avg <= 5){
            this.priceCategory = "$";
        }else if(avg <= 15){
            this.priceCategory =  "$$";
        }else{
            this.priceCategory = "$$$";
        }
    }

    public void applyRating(int rating){
        double total = stars * noOfVotes + rating;
        this.stars = total/ (noOfVotes + 1);
    }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getFoodCategory() { return foodCategory; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }

    public double getStars() { return stars; }
    public void setStars(double stars) { this.stars = stars; }

    public int getNoOfVotes() { return noOfVotes; }
    public void setNoOfVotes(int noOfVotes) { this.noOfVotes = noOfVotes; }

    public String getStoreLogo() { return storeLogo; }
    public void setStoreLogo(String storeLogo) { this.storeLogo = storeLogo; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public Product getProduct(String productName) {
        Product prod = null;
        for(Product product : products){
            if(product.getProductName().equals(productName)){
                return product;
            }
        }
        return prod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Store store = (Store) o;

        return Double.compare(store.latitude, latitude) == 0 &&
                Double.compare(store.longitude, longitude) == 0 &&
                storeName.equals(store.storeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeName, latitude, longitude);
    }


}