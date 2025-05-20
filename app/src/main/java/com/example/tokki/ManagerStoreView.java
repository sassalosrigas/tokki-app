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

import com.example.tokki.R;
import com.example.tokki.StoreAdapter;
import com.example.tokki.java.Customer;
import com.example.tokki.java.Store;

import java.util.ArrayList;
import java.util.List;

public class ManagerStoreView extends AppCompatActivity {

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
                    Toast.makeText(ManagerStoreView.this, "No stores found nearby", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Store clickedStore = nearbyStores.get(position);

            Toast.makeText(ManagerStoreView.this,
                    "Selected: " + clickedStore.getStoreName(),
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ManagerStoreView.this, CustomerStoreView.class);
            intent.putExtra("STORE", clickedStore);
            startActivity(intent);
        });
    }
}