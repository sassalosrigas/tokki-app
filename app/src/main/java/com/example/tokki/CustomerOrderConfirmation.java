package com.example.tokki;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tokki.java.Customer;
import com.example.tokki.java.Order;
import com.example.tokki.java.Store;

public class CustomerOrderConfirmation extends AppCompatActivity{

    private Order order;
    private Customer customer;
    private OrderConfirmationProductAdapter reservedProductAdapter;
    private ListView reservedProductsListView;

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

        order = (Order) getIntent().getSerializableExtra("ORDER_DATA");
        customer = new Customer("rigas", "123", 37.986633, 23.734900);

        if (order != null) {
            for (int i = 0; i < order.getProducts().size(); i++) {
                Log.d("Order", order.getProducts().get(i).getProductName() +
                        " x" + order.getQuantities().get(i));
            }
            Log.d("Order", "Total: €" + order.getTotal());
        }

        TextView totalText = findViewById(R.id.total);

        double totalAmount = order.getTotal(); // Ensure 'order' is initialized before this
        String formatted = String.format("total: €%.2f", totalAmount);
        totalText.setText(formatted);

        reservedProductsListView = findViewById(R.id.order_list_view);
        reservedProductAdapter = new OrderConfirmationProductAdapter(this, order.getProducts(), order.getQuantities());
        reservedProductsListView.setAdapter(reservedProductAdapter);

        findViewById(R.id.clear_cart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order != null) {
                    order.getProducts().clear();
                    order.getQuantities().clear();
                    reservedProductAdapter.notifyDataSetChanged();

                    TextView totalText = findViewById(R.id.total);
                    totalText.setText("total: €0.00");

                    Toast.makeText(CustomerOrderConfirmation.this, "Cart cleared.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
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