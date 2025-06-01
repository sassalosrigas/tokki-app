package com.example.tokki;

import static java.lang.Thread.sleep;

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
import com.example.tokki.java.Manager;
import com.example.tokki.java.Master;
import com.example.tokki.java.Worker;
import com.example.tokki.java.WorkerFunctions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //kodikas gia na treksei topika sto emulator sthn idia siskeuh gia testing
        Master master = new Master();
        master.openServer();
        try {
            sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Worker worker = new Worker(8081);
        Master.getWorkers().add(worker);
        worker.start();
        Master.rebalanceStores();
        try {
            String[] files = getAssets().list("");
            Log.d("Assets", "All files: " + Arrays.toString(files));
        } catch (IOException e) {
            Log.e("Assets", "Error listing assets", e);
        }
        new Thread(() -> {
            Socket masterSocket = null;  // Connect to Master
            /*
            kodikas gia na sindethei se ena server pou trexei eksoterika/se allh siskeuh
            try {
                masterSocket = new Socket("127.0.0.1", 8080);
                ObjectOutputStream out = null;
                out = new ObjectOutputStream(masterSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(masterSocket.getInputStream());
                out.writeObject(new WorkerFunctions("REGISTER"));
                out.flush();
            } catch (RuntimeException | IOException e) {
                throw new RuntimeException(e);
            }*/
            boolean isAdded = Manager.addStore(MainActivity.this, "store.json");
            Manager.addStore(MainActivity.this, "store2.json");
            Manager.addStore(MainActivity.this, "store3.json");
            Manager.addStore(MainActivity.this, "store4.json");
            Manager.addStore(MainActivity.this, "store5.json");
            Manager.addStore(MainActivity.this, "store6.json");
            Manager.addStore(MainActivity.this, "store7.json");
            runOnUiThread(() -> {
                if (isAdded) {
                    Toast.makeText(MainActivity.this, "Store added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add store", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
        findViewById(R.id.customerbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Entering Customer", Toast.LENGTH_SHORT).show();
                Customer customer = new Customer("rigas", "123", 37.986633, 23.734900);
                Intent intent = new Intent(MainActivity.this, CustomerMain.class);
                intent.putExtra("CUSTOMER", customer);
                startActivity(intent);
            }
        });

        findViewById(R.id.managerbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Entering Manager", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ManagerMain.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            }
        });
    }
}