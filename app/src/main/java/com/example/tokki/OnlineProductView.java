package com.example.tokki;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.example.tokki.java.Manager;
import com.example.tokki.java.Product;
import com.example.tokki.java.Store;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;

public class OnlineProductView extends AppCompatActivity implements ManagerProductRemovalAdapter.ManagerProductRemove{

    private ImageView storeLogo;
    private TextView storeTitle;
    private TextView storeCategory;
    private TextView storeRating;
    private TextView storePrice;

    private ListView productsListView;
    private BaseAdapter productAdapter;

    private List<Product> products;

    //@SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "Opened OnlineProductView", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_product_view);
        boolean showSwitch = getIntent().getBooleanExtra("SHOW_SWITCH", false);
        storeLogo = findViewById(R.id.store_logo2);
        storeTitle = findViewById(R.id.store_tittle);
        storeCategory = findViewById(R.id.store_category_main);
        storeRating = findViewById(R.id.store_rating);
        storePrice = findViewById(R.id.store_price);

        Store store = (Store) getIntent().getSerializableExtra("STORE");

        if (store != null) {
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

            findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                }
            });

            new Thread(() -> {
                try {
                    products = Manager.getOnlineProducts(store);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(() -> {
                    if (products!=null) {
                        Toast.makeText(OnlineProductView.this, "Listed all online products", Toast.LENGTH_SHORT).show();
                        if (showSwitch) {
                            productAdapter = new ManagerProductRemovalAdapter(this, products,this);
                        }else{
                            productAdapter = new ManagerProductAdapter(this,products);
                            productsListView.setOnItemClickListener((parent, view, position, id) -> {
                                Product selectedProduct = (Product) parent.getItemAtPosition(position);
                                Toast.makeText(this, "Clickara", Toast.LENGTH_SHORT).show();
                                LayoutInflater inflater = getLayoutInflater();
                                View popupView = inflater.inflate(R.layout.popup_enteramount, null);

                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setView(popupView);

                                AlertDialog dialog = builder.create();
                                dialog.show();

                                MaterialButton addBtn = popupView.findViewById(R.id.addBtn);
                                MaterialButton cancelBtn = popupView.findViewById(R.id.cancelBtn);
                                TextInputEditText input = popupView.findViewById(R.id.storeNameEditText);

                                addBtn.setOnClickListener(bv -> {
                                    new Thread(() -> {
                                        try {
                                            boolean changed = Manager.modifyAvailability(store, selectedProduct, Integer.parseInt(input.getText().toString()));
                                            runOnUiThread(() -> {
                                                if (changed) {
                                                    Toast.makeText(this, "new amount set", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        } catch (ClassNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).start();
                                    dialog.dismiss();
                                });
                                cancelBtn.setOnClickListener(bv -> dialog.dismiss());
                            });
                        }
                        productsListView.setAdapter( productAdapter);
                    } else {
                        Toast.makeText(OnlineProductView.this, "No online products exist", Toast.LENGTH_SHORT).show();
                    }
                });

            }).start();
            productsListView = findViewById(R.id.products_list_view);

        } else {
            Toast.makeText(this, "Store data not available", Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    @Override
    public void onSwitchFlipped(int position) throws IOException, ClassNotFoundException {
        Toast.makeText(OnlineProductView.this, "FLIPPED", Toast.LENGTH_SHORT).show();
        Product product = (Product) productAdapter.getItem(position);
        Store store = (Store) getIntent().getSerializableExtra("STORE");
        new Thread(() -> {
            boolean removed = false;
            try {
                removed = Manager.removeProductFromStore(store,product);
                boolean finalRemoved = removed;
                runOnUiThread(() -> {
                    if (finalRemoved) {
                        Toast.makeText(OnlineProductView.this, "Product removed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OnlineProductView.this, "Failed to remove product", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}