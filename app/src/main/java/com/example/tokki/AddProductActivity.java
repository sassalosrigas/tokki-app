package com.example.tokki;
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
        EditText descriptionInput = findViewById(R.id.inputDescription);
        EditText priceInput = findViewById(R.id.inputPrice);
        EditText categoryInput = findViewById(R.id.inputCategory);
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
            String desc = descriptionInput.getText().toString().trim();
            String price = priceInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();

            if (name.isEmpty() || desc.isEmpty() || price.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }else{
                //Product product = new Product(name, desc, Integer.parseInt(price.toString()),category));
            }
            Toast.makeText(this, "Submitted: " + name, Toast.LENGTH_SHORT).show();
        });
    }
}