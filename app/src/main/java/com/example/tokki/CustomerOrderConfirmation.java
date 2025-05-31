package com.example.tokki;

import static android.app.ProgressDialog.show;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tokki.java.Customer;
import com.example.tokki.java.Order;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;


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

        double totalAmount = order.getTotal();
        String formatted = String.format("total: €%.2f", totalAmount);
        totalText.setText(formatted);

        reservedProductsListView = findViewById(R.id.order_list_view);
        reservedProductAdapter = new OrderConfirmationProductAdapter(this, order.getProducts(), order.getQuantities());
        reservedProductsListView.setAdapter(reservedProductAdapter);

        findViewById(R.id.clear_cart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order != null) {
                    View listView = findViewById(R.id.order_list_view);
                    listView.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> {
                                order.getProducts().clear();
                                order.getQuantities().clear();
                                reservedProductAdapter.notifyDataSetChanged();
                                listView.setAlpha(1f);
                            })
                            .start();

                    TextView totalText = findViewById(R.id.total);
                    totalText.setText("total: €0.00");

                    Snackbar.make(findViewById(R.id.main), "Cart cleared.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order == null) {
                    new AlertDialog.Builder(CustomerOrderConfirmation.this, R.style.AlertDialogCustom)
                            .setTitle("Error")
                            .setMessage("Order data not available.")
                            .setPositiveButton("OK", null)
                            .show();
                }
                v.setEnabled(false);

                Snackbar.make(findViewById(R.id.main), "Submitting your order...", Snackbar.LENGTH_SHORT).show();


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
                            LottieAnimationView anim = findViewById(R.id.anim);
                            anim.setAnimation("tick.json");
                            anim.setVisibility(View.VISIBLE);
                            anim.addAnimatorListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    Intent intent = new Intent(CustomerOrderConfirmation.this, CustomerMain.class);
                                    startActivity(intent);
                                    finish();
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                }
                            });

                            anim.playAnimation();
                        } else {
                            LottieAnimationView anim = findViewById(R.id.anim);
                            anim.setAnimation("fail.json");
                            anim.setVisibility(View.VISIBLE);
                            anim.addAnimatorListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    new AlertDialog.Builder(CustomerOrderConfirmation.this, R.style.AlertDialogCustom)
                                            .setTitle("Order Failed")
                                            .setMessage("Some products may be out of stock. Please try again.")
                                            .setPositiveButton("OK", (dialog, which) -> {
                                                Intent intent = new Intent(CustomerOrderConfirmation.this,
                                                        CustomerStoreView.class);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .show();
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                }
                            });

                            anim.playAnimation();
                            new Thread(() -> {
                                try {
                                    customer.rollbackPurchase(order.getStore()
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();


                        }
                    });
                }).start();
            }
        });
    }
}