package com.example.tokki;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductView extends AppCompatActivity{

    private ImageView storeLogo;
    private TextView storeTitle;
    private TextView storeCategory;
    private TextView storeRating;
    private TextView storePrice;

    private ListView productsListView;
    private BaseAdapter productAdapter;

    private List<Product> products;

    private Map<String,Integer> sales;

    private String function;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Opened ProductView", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_product_view);
        storeLogo = findViewById(R.id.store_logo2);
        storeTitle = findViewById(R.id.store_tittle);
        storeCategory = findViewById(R.id.store_category_main);
        storeRating = findViewById(R.id.store_rating);
        storePrice = findViewById(R.id.store_price);

        Store store = (Store) getIntent().getSerializableExtra("STORE");
        function = (String) getIntent().getSerializableExtra("FUNCTION");

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

            findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                }
            });


            new Thread(() -> {
                try {
                    if (function.equals("SALES_PER_PRODUCT")) {
                        sales = Manager.sppStore(store);
                    } else if (function.equals("SALES_PER_STORE_CATEGORY")) {
                        String category = (String) getIntent().getSerializableExtra("CATEGORY");
                        sales = Manager.spsCategory(category);
                    } else {
                        String category = (String) getIntent().getSerializableExtra("CATEGORY");
                        sales = Manager.sppCategory(category);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(() -> {
                    if (sales != null) {
                        Toast.makeText(ProductView.this, "Listed all products", Toast.LENGTH_SHORT).show();
                        Map<String, Integer> orderedSales = new LinkedHashMap<>();


                        orderedSales.putAll(sales);
                        int total = orderedSales.values().stream().mapToInt(Integer::intValue).sum();
                        orderedSales.put("TOTAL", total);
                        productAdapter = new StatisticsProductAdapter(this, orderedSales, "product_in_store");
                        productsListView.setAdapter(productAdapter);
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ProductView.this, "No products exist", Toast.LENGTH_SHORT).show();
                    }
                });

            }).start();
            productsListView = findViewById(R.id.products_list_view);

        } else {
            //Toast.makeText(this, "Store data not available", Toast.LENGTH_SHORT).show();
            //finish();
            findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                }
            });
            storeLogo.setVisibility(View.GONE);
            storeTitle.setVisibility(View.GONE);
            storeCategory.setVisibility(View.GONE);
            storeRating.setVisibility(View.GONE);
            storePrice.setVisibility(View.GONE);
            new Thread(()-> {
                if (function.equals("SALES_PER_STORE_CATEGORY")) {
                    String category = (String) getIntent().getSerializableExtra("CATEGORY");
                    try {
                        sales = Manager.spsCategory(category);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String category = (String) getIntent().getSerializableExtra("CATEGORY");
                    try {
                        sales = Manager.sppCategory(category);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                runOnUiThread(() -> {
                    if (sales != null) {
                        Toast.makeText(ProductView.this, "Listed all products", Toast.LENGTH_SHORT).show();
                        Map<String, Integer> orderedSales = new LinkedHashMap<>();


                        orderedSales.putAll(sales);
                        int total = orderedSales.values().stream().mapToInt(Integer::intValue).sum();
                        orderedSales.put("TOTAL", total);
                        productAdapter = new StatisticsProductAdapter(this, orderedSales, "product_in_store");
                        productsListView.setAdapter(productAdapter);
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ProductView.this, "No products exist", Toast.LENGTH_SHORT).show();
                    }
                });

            }).start();
            productsListView = findViewById(R.id.products_list_view);
        }

    }
}