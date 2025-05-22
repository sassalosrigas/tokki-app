package com.example.tokki;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.example.tokki.java.Manager;
import com.example.tokki.java.Product;
import com.example.tokki.java.Store;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;

public class OnlineProductView extends AppCompatActivity {

    private ImageView storeLogo;
    private TextView storeTitle;
    private TextView storeCategory;
    private TextView storeRating;
    private TextView storePrice;

    private ListView productsListView;
    private ManagerProductRemovalAdapter productAdapter;

    private List<Product> products;
    //@SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Opened OnlineProductView", Toast.LENGTH_SHORT).show();
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


            new Thread(() -> {
                products = store.getProducts();
                runOnUiThread(() -> {
                    if (products!=null) {
                        Toast.makeText(OnlineProductView.this, "Listed all offline products", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OnlineProductView.this, "No offline products exist", Toast.LENGTH_SHORT).show();
                    }
                });

            }).start();
            productsListView = findViewById(R.id.products_list_view);
            productAdapter = new ManagerProductRemovalAdapter(this, products);
            productsListView.setAdapter(productAdapter);
            productsListView.post(() -> {
                for (int i = 0; i < productsListView.getChildCount(); i++) {
                    View row = productsListView.getChildAt(i);

                    Product product = products.get(i); // or use final int i = inside a loop

                }
            });
        } else {
            Toast.makeText(this, "Store data not available", Toast.LENGTH_SHORT).show();
            finish();
        }


    }
}