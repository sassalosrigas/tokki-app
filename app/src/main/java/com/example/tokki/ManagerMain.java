package com.example.tokki;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tokki.java.Manager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ManagerMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manager_homepage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.addstore_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View popupView = inflater.inflate(R.layout.popup_add_store, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ManagerMain.this);
                builder.setView(popupView);

                AlertDialog dialog = builder.create();
                dialog.show();

                MaterialButton addBtn = popupView.findViewById(R.id.addBtn);
                MaterialButton cancelBtn = popupView.findViewById(R.id.cancelBtn);
                TextInputEditText input = popupView.findViewById(R.id.storeNameEditText);

                addBtn.setOnClickListener(bv -> {
                    String storeName = input.getText().toString();
                    new Thread(() -> {
                        boolean isAdded = Manager.addStore(ManagerMain.this, storeName);

                        runOnUiThread(() -> {
                            if (isAdded) {
                                Toast.makeText(ManagerMain.this, "Store added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ManagerMain.this, "Failed to add store", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                    dialog.dismiss();
                });
                cancelBtn.setOnClickListener(bv -> dialog.dismiss());
            }
        });

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            }
        });

        findViewById(R.id.addproduct_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerMain.this, ManagerStoreView.class);
                intent.putExtra("FUNCTION", "ADD_PRODUCT");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        findViewById(R.id.removeproduct_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerMain.this, ManagerStoreView.class);
                intent.putExtra("FUNCTION", "REMOVE_PRODUCT");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        findViewById(R.id.modifystock_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerMain.this, ManagerStoreView.class);
                intent.putExtra("FUNCTION","MODIFY_STOCK");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        findViewById(R.id.stats_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerMain.this, StatisticsPage.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}