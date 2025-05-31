package com.example.tokki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tokki.java.Manager;

import java.io.IOException;
import java.util.List;

public class ManagerStatisticsFormat extends AppCompatActivity {

    private List<String> categories;

    private ListView categoriesListView;

    private BaseAdapter categoryAdapter;

    String function;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_statistics_format);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        function = (String) getIntent().getSerializableExtra("FUNCTION");
        categoriesListView = findViewById(R.id.category_item);
        new Thread(() -> {
            try {
                if(function.equals("SALES_PER_STORE_CATEGORY")){
                    categories = Manager.getAllStoreCategories();
                }else{
                    categories = Manager.getAllProductCategories();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            runOnUiThread(() -> {
                if(categories!=null){
                    categoryAdapter = new StringListAdapter(this, categories);
                    categoriesListView.setAdapter(categoryAdapter);
                    categoryAdapter.notifyDataSetChanged();
                    categoriesListView.setOnItemClickListener((parent, view, position, id) -> {
                        String clickedItem = categories.get(position);
                        Toast.makeText(this, "Clicked: " + clickedItem, Toast.LENGTH_SHORT).show();
                        if(function.equals("SALES_PER_STORE_CATEGORY")){
                            Intent intent = new Intent(ManagerStatisticsFormat.this, ProductView.class);
                            intent.putExtra("FUNCTION", "SALES_PER_STORE_CATEGORY");
                            intent.putExtra("CATEGORY", clickedItem);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(ManagerStatisticsFormat.this, ProductView.class);
                            intent.putExtra("FUNCTION", "SALES_PER_PRODUCT_CATEGORY");
                            intent.putExtra("CATEGORY", clickedItem);
                            startActivity(intent);
                        }
                    });
                }
            });
        }).start();

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });


    }
}