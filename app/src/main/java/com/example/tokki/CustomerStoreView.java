package com.example.tokki;
import com.example.tokki.java.Order;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.tokki.java.Product;
import com.example.tokki.java.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerStoreView extends AppCompatActivity implements ProductAdapter.OnQuantityChangeListener{

    private ImageView storeLogo;
    private TextView storeTitle;
    private TextView storeCategory;
    private TextView storeRating;
    private TextView storePrice;
    private CardView storeButton;

    private ListView productsListView;
    private ProductAdapter productAdapter;

    private List<Integer> quantities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_store_view);

        storeLogo = findViewById(R.id.store_logo2);
        storeTitle = findViewById(R.id.store_tittle);
        storeCategory = findViewById(R.id.store_category_main);
        storeRating = findViewById(R.id.store_rating);
        storePrice = findViewById(R.id.store_price);
        storeButton = findViewById(R.id.store_button);

        Store store = (Store) getIntent().getSerializableExtra("STORE");

        if (store != null) {
            quantities = new ArrayList<>(Collections.nCopies(store.getProducts().size(), 0));
            storeTitle.setText(store.getStoreName());
            storeCategory.setText(store.getFoodCategory());
            storeRating.setText(String.format("★ %.1f", store.getStars()));
            storePrice.setText(store.getPriceCategory());


            String logoName = store.getStoreLogo();
            int resId = getResources().getIdentifier(logoName, "drawable", getPackageName());
            if (resId != 0) {
                storeLogo.setImageResource(resId);
            } else {
                storeLogo.setImageResource(R.drawable.img);
            }

            productsListView = findViewById(R.id.products_list_view);
            productAdapter = new ProductAdapter(this, store.getProducts(), this);
            productsListView.setAdapter(productAdapter);

        /*
        productsListView.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = (Product) parent.getItemAtPosition(position);
        });

         */
            findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent intent = new Intent(CustomerStoreView.this, CustomerMain.class);
                    //startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
                }
            });

            findViewById(R.id.store_button).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    List<Product> orderedProducts = new ArrayList<>();
                    List<Integer> orderedQuantities = new ArrayList<>();

                    for (int i = 0; i < quantities.size(); i++) {
                        if (quantities.get(i) > 0) {
                            orderedProducts.add((Product) productAdapter.getItem(i));
                            orderedQuantities.add(quantities.get(i));
                        }
                    }

                    if (orderedProducts.isEmpty()) {
                        Toast.makeText(CustomerStoreView.this,
                                "Please select at least one product",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Order order = new Order(store, orderedProducts, orderedQuantities);

                    Intent intent = new Intent(CustomerStoreView.this, CustomerOrderConfirmation.class);
                    intent.putExtra("ORDER_DATA", order);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        } else {
            Toast.makeText(this, "Store data not available", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    public void onQuantityChanged(int position, int newQuantity) {
        Product product = (Product) productAdapter.getItem(position);
        quantities.set(position, newQuantity);
        Toast.makeText(this,
                "Quantity changed for " + product.getProductName() + ": " + newQuantity,
                Toast.LENGTH_SHORT).show();

    }
}