package com.example.tokki;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tokki.java.Customer;
import com.example.tokki.java.Order;
import com.example.tokki.java.Product;
import com.example.tokki.java.Store;

import java.io.IOException;
import java.util.List;

public class CustomerOrderConfirmation extends AppCompatActivity {

    private Order order;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_order_confirmation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize order and customer
        order = (Order) getIntent().getSerializableExtra("ORDER_DATA");
        customer = new Customer("rigas", "123", 37.986633, 23.734900);

        if (order != null) {
            for (int i = 0; i < order.getProducts().size(); i++) {
                Log.d("Order", order.getProducts().get(i).getProductName() +
                        " x" + order.getQuantities().get(i));
            }
            Log.d("Order", "Total: €" + order.getTotal());
        }

        findViewById(R.id.store_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order == null) {
                    Toast.makeText(CustomerOrderConfirmation.this,
                            "Order data not available",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                v.setEnabled(false);

                Toast.makeText(CustomerOrderConfirmation.this,
                        "Processing your order...",
                        Toast.LENGTH_SHORT).show();

                new Thread(() -> {
                    boolean allReservationsSuccessful = true;
                    boolean purchaseCompleted = false;

                    for (int i = 0; i < order.getProducts().size(); i++) {
                        try {
                            boolean reserved = customer.reserveProduct(
                                    order.getProducts().get(i),
                                    order.getStore(),
                                    order.getQuantities().get(i)
                            );

                            if (!reserved) {
                                allReservationsSuccessful = false;
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            allReservationsSuccessful = false;
                            break;
                        }
                    }
                    if (allReservationsSuccessful) {
                        try {
                            purchaseCompleted = customer.completePurchase(order.getStore());
                        } catch (Exception e) {
                            e.printStackTrace();
                            purchaseCompleted = false;
                        }
                    }

                    // 3. Handle result on UI thread
                    boolean finalPurchaseCompleted = purchaseCompleted;
                    runOnUiThread(() -> {
                        if (finalPurchaseCompleted) {
                            Toast.makeText(CustomerOrderConfirmation.this,
                                    "Order completed successfully!",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CustomerOrderConfirmation.this,
                                    CustomerMain.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Rollback reservations if failed
                            new Thread(() -> {
                                try {
                                    customer.rollbackPurchase(order.getStore()
                                    );
                                    Intent intent = new Intent(CustomerOrderConfirmation.this,
                                            CustomerMain.class);
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();

                            Toast.makeText(CustomerOrderConfirmation.this,
                                    "Order failed. Please try again.",
                                    Toast.LENGTH_LONG).show();
                            v.setEnabled(true);
                        }
                    });
                }).start();
            }
        });
    }
}