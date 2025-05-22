package com.example.tokki;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tokki.R;
import com.example.tokki.StoreAdapter;
import com.example.tokki.java.Customer;
import com.example.tokki.java.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomerMain extends AppCompatActivity {

    private ListView listView;
    private StoreAdapter storeAdapter;
    private List<Store> nearbyStores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.storesListView);
        storeAdapter = new StoreAdapter(this, nearbyStores);
        listView.setAdapter(storeAdapter);

        Customer customer = new Customer("rigas", "123", 37.986633, 23.734900);

        new Thread(() -> {
            List<Store> stores = customer.showNearbyStores();
            runOnUiThread(() -> {
                if (stores != null && !stores.isEmpty()) {
                    nearbyStores.clear();
                    nearbyStores.addAll(stores);
                    storeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CustomerMain.this, "No stores found nearby", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();

        Button filterButton = findViewById(R.id.button_filter);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(CustomerMain.this);
                View dialogView = inflater.inflate(R.layout.filter_popup, null);

                EditText categoryInput = dialogView.findViewById(R.id.input_category);
                List<ImageView> minStars = Arrays.asList(
                        dialogView.findViewById(R.id.min_star1),
                        dialogView.findViewById(R.id.min_star2),
                        dialogView.findViewById(R.id.min_star3),
                        dialogView.findViewById(R.id.min_star4),
                        dialogView.findViewById(R.id.min_star5)
                );

                List<ImageView> maxStars = Arrays.asList(
                        dialogView.findViewById(R.id.max_star1),
                        dialogView.findViewById(R.id.max_star2),
                        dialogView.findViewById(R.id.max_star3),
                        dialogView.findViewById(R.id.max_star4),
                        dialogView.findViewById(R.id.max_star5)
                );

                final int[] selectedMinRating = {0};
                final int[] selectedMaxRating = {5};

                for (int i = 0; i < minStars.size(); i++) {
                    final int rating = i + 1;
                    minStars.get(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedMinRating[0] = rating;
                            updateStars(minStars, rating);
                        }
                    });
                }

                for (int i = 0; i < maxStars.size(); i++) {
                    final int rating = i + 1;
                    maxStars.get(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedMaxRating[0] = rating;
                            updateStars(maxStars, rating);
                        }
                    });
                }

                RadioGroup priceGroup = dialogView.findViewById(R.id.price_filter_group);

                AlertDialog dialog = new AlertDialog.Builder(CustomerMain.this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

                Button cancelButton = dialogView.findViewById(R.id.button_cancel);
                Button applyButton = dialogView.findViewById(R.id.button_apply);

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                Customer customer = new Customer("rigas", "123", 37.986633, 23.734900);
                applyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String category = categoryInput.getText().toString().trim();
                        int minStars = selectedMinRating[0];
                        int maxStars = selectedMaxRating[0];
                        int priceLevel;

                        int checkedId = priceGroup.getCheckedRadioButtonId();
                        String prange;
                        if (checkedId == R.id.price_1) {
                            priceLevel = 1;
                            prange = "$";
                        } else if (checkedId == R.id.price_2) {
                            priceLevel = 2;
                            prange = "$$";
                        } else if (checkedId == R.id.price_3) {
                            priceLevel = 3;
                            prange = "$$$";
                        } else {
                            priceLevel = 0;
                            prange = "";
                        }
                        new Thread(() -> {
                            Log.d("Category", category);
                            List<Store> stores = customer.filterStores(
                                    category,
                                    minStars,
                                    maxStars,
                                    prange
                            );

                            runOnUiThread(() -> {
                                if (stores != null && !stores.isEmpty()) {
                                    nearbyStores.clear();
                                    nearbyStores.addAll(stores);
                                    storeAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(
                                            CustomerMain.this,
                                            "No stores match your filters",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                        }).start(); // Don't forget to start the thread!
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }

            private void updateStars(List<ImageView> stars, int count) {
                for (int i = 0; i < stars.size(); i++) {
                    if (i < count) {
                        stars.get(i).setImageResource(R.drawable.star_filled);
                    } else {
                        stars.get(i).setImageResource(R.drawable.star_outline);
                    }
                }
            }

        });


        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the clicked store from your ArrayList
            Store clickedStore = nearbyStores.get(position);

            Toast.makeText(CustomerMain.this,
                    "Selected: " + clickedStore.getStoreName(),
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CustomerMain.this, CustomerStoreView.class);
            intent.putExtra("STORE", clickedStore);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });
    }
}