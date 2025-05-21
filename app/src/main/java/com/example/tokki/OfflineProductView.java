package com.example.tokki;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.tokki.java.Product;
import com.example.tokki.java.Store;

public class OfflineProductView extends AppCompatActivity {

    private ImageView storeLogo;
    private TextView storeTitle;
    private TextView storeCategory;
    private TextView storeRating;
    private TextView storePrice;

    private ListView productsListView;
    private ManagerProductAdapter productAdapter;

    //@SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Opened OfflineProductView", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_product_view);

        storeLogo = findViewById(R.id.store_logo2);
        storeTitle = findViewById(R.id.store_tittle);
        storeCategory = findViewById(R.id.store_category_main);
        storeRating = findViewById(R.id.store_rating);
        storePrice = findViewById(R.id.store_price);

        Store store = (Store) getIntent().getSerializableExtra("STORE");

        if (store != null) {
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
            productAdapter = new ManagerProductAdapter(this, store.getProducts());
            productsListView.setAdapter(productAdapter);

            productsListView.setOnItemClickListener((parent, view, position, id) -> {
                Product selectedProduct = (Product) parent.getItemAtPosition(position);
            });
        } else {
            Toast.makeText(this, "Store data not available", Toast.LENGTH_SHORT).show();
            finish();
        }


    }
}