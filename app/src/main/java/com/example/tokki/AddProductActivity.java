package com.example.tokki;
import com.example.tokki.java.Manager;
import com.example.tokki.java.Order;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.tokki.java.Product;
import com.example.tokki.java.Store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private ImageView storeLogo;
    private TextView storeTitle;
    private TextView storeCategory;
    private TextView storeRating;
    private TextView storePrice;
    private CardView storeButton;

    private ListView productsListView;
    private ProductAdapter productAdapter;

    private List<Integer> quantities;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        EditText nameInput = findViewById(R.id.inputName);
        EditText categoryInput = findViewById(R.id.inputCategory);
        EditText priceInput = findViewById(R.id.inputPrice);
        EditText stockInput = findViewById(R.id.inputStock);
        Button submitButton = findViewById(R.id.submitButton);

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

        }


        submitButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String stock = stockInput.getText().toString().trim();
            String price = priceInput.getText().toString().trim();

            if (name.isEmpty() || category.isEmpty() || price.isEmpty() || stock.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }else{
                Product product = new Product(name, category,Integer.parseInt(stock.toString()) ,Integer.parseInt(price.toString()));
                new Thread(()->
                {
                    try {
                        boolean added = Manager.addProductToStore(store,product);
                        runOnUiThread(()-> {
                            if (added) {
                                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
            Toast.makeText(this, "Submitted: " + name, Toast.LENGTH_SHORT).show();
        });
    }
}