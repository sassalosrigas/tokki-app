package com.example.tokki;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tokki.java.Store;
import com.example.tokki.java.Manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManagerStoreView extends AppCompatActivity{

    private ListView listView;
    private StoreAdapter storeAdapter;
    private List<Store> allStores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_viewstores);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String function = (String) getIntent().getSerializableExtra("FUNCTION");

        listView = findViewById(R.id.storesListView);
        storeAdapter = new StoreAdapter(this, allStores);
        listView.setAdapter(storeAdapter);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        new Thread(() -> {
            try {
                final List<Store> stores = Manager.showAllStores();
                runOnUiThread(() -> {
                    if (stores != null && !stores.isEmpty()) {
                        allStores.clear();
                        allStores.addAll(stores);
                        storeAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ManagerStoreView.this, "No stores found nearby", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Store clickedStore = allStores.get(position);

            if(function.equals("ADD_PRODUCT")) {
                new androidx.appcompat.app.AlertDialog.Builder(ManagerStoreView.this)
                        .setTitle("Choose Action")
                        .setMessage("What would you like to do with " + clickedStore.getStoreName() + "?")
                        .setPositiveButton("Add New Product", (dialog, which) -> {
                            Intent intent = new Intent(ManagerStoreView.this, AddProductActivity.class);
                            intent.putExtra("STORE", clickedStore);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                        })
                        .setNegativeButton("View Offline Products", (dialog, which) -> {
                            Intent intent = new Intent(ManagerStoreView.this, OfflineProductView.class);
                            intent.putExtra("STORE", clickedStore);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                        })
                        .setNeutralButton("Cancel", null)
                        .show();
            } else if (function.equals("REMOVE_PRODUCT")) {
                Intent intent = new Intent(ManagerStoreView.this, OnlineProductView.class);
                intent.putExtra("STORE", clickedStore);
                intent.putExtra("SHOW_SWITCH", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            }else if (function.equals("MODIFY_STOCK")){
                Intent intent = new Intent(ManagerStoreView.this, OnlineProductView.class);
                intent.putExtra("STORE", clickedStore);
                intent.putExtra("SHOW_SWITCH", false);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            } else if (function.equals("SALES_PER_PRODUCT")) {
                Intent intent = new Intent(ManagerStoreView.this, ProductView.class);
                intent.putExtra("STORE", clickedStore);
                intent.putExtra("FUNCTION", "SALES_PER_PRODUCT");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            }
            /*
            else if (function.equals("SALES_PER_PRODUCT_CATEGORY")){
                Intent intent = new Intent(ManagerStoreView.this, ProductView.class);
                intent.putExtra("STORE", clickedStore);
                intent.putExtra("FUNCTION", "SALES_PER_PRODUCT_CATEGORY");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            }else if (function.equals("SALES_PER_STORE_CATEGORY")){
                Intent intent = new Intent(ManagerStoreView.this, ProductView.class);
                intent.putExtra("STORE", clickedStore);
                intent.putExtra("FUNCTION", "SALES_PER_STORE_CATEGORY");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            }

             */
        });
    }
}