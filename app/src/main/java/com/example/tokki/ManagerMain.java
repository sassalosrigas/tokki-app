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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ManagerMain extends AppCompatActivity {
    View popupView;
    AlertDialog dialog;


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
                Toast.makeText(ManagerMain.this, "Entering Add Store", Toast.LENGTH_SHORT).show();
                LayoutInflater inflater = getLayoutInflater();
                View popupView = inflater.inflate(R.layout.popup_add_store, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ManagerMain.this);
                builder.setView(popupView);

                // Show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();

                MaterialButton addBtn = popupView.findViewById(R.id.addBtn);
                MaterialButton cancelBtn = popupView.findViewById(R.id.cancelBtn);
                TextInputEditText input = popupView.findViewById(R.id.storeNameEditText);

                // Add button behavior
                addBtn.setOnClickListener(bv -> {
                    String storeName = input.getText().toString();
                    // Do something with the input
                    Toast.makeText(ManagerMain.this, "Store added: " + storeName, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

                // Cancel button behavior
                cancelBtn.setOnClickListener(bv -> dialog.dismiss());
            }
        });



        findViewById(R.id.addproduct_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManagerMain.this, "Entering Add Product", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManagerMain.this, AddProductPage.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.removeproduct_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManagerMain.this, "Entering Remove Product", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManagerMain.this, RemoveProductPage.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.modifystock_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManagerMain.this, "Entering Modify Stock", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManagerMain.this, ModifyStockPage.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.stats_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManagerMain.this, "Entering Statistics", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManagerMain.this, StatisticsPage.class);
                startActivity(intent);
            }
        });
    }
}