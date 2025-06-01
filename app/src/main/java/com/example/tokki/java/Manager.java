package com.example.tokki.java;
import static java.lang.System.in;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Manager{

    public static boolean addStore(Context context, String filename) {
        Log.d("Manager", "Attempting to add store from file: " + filename);
            try {
                Store newStore = JsonHandler.readStoreFromAssets(context, filename);
                newStore.setFilepath(filename);

                Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                out.writeObject(new WorkerFunctions("ADD_STORE", newStore));
                out.flush();

                Object response = in.readObject();

                if (response instanceof Store) {
                    Log.d("AddStore", "Store " + newStore.getStoreName() + " added successfully.");
                    out.close();
                    in.close();
                    socket.close();
                    return true;
                } else {
                    Log.d("AddStore", "Response: " + response);
                }

                out.close();
                in.close();
                socket.close();
            } catch (Exception e) {
                Log.e("AddStore", "Error adding store: " + e.getMessage(), e);
            }
        return false;
    }

    public static Product addProduct(Scanner input){
        /*
            Diadikasia eisodou kai dhmiourgias proiontos
         */
        try{
            System.out.println("Give product name:");
            String productName = input.nextLine();
            System.out.println("Give product type:");
            String prodType = input.nextLine();
            System.out.println("Give product price:");
            int price = input.nextInt();
            System.out.println("Give available amount:");
            int amount = input.nextInt();
            return new Product(productName, prodType, price, amount);

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean removeProductFromStore(Store store, Product product) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(new WorkerFunctions("REMOVE_PRODUCT",store,product));
        out.flush();
        Object response = in.readObject();
        if(response instanceof String){
            System.out.println("Server response: " + response);
        }
        out.close();
        in.close();
        socket.close();
        return true;
    }

    public static List<Product> getOfflineProducts(Store store) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(new WorkerFunctions("GET_OFFLINE_PRODUCTS", store));
        out.flush();
        Object response = in.readObject();
        ArrayList<Product> products = null;
        if(response instanceof ArrayList){
            products = (ArrayList<Product>) response;
        }
        out.close();
        in.close();
        socket.close();
        return products;
    }

    public static List<Product> getOnlineProducts(Store store) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(new WorkerFunctions("GET_ONLINE_PRODUCTS", store));
        out.flush();
        Object response = in.readObject();
        ArrayList<Product> products = null;
        if(response instanceof ArrayList){
            products = (ArrayList<Product>) response;
            Log.d("ONLINE", "Got products size "+ products.size());
        }
        out.close();
        in.close();
        socket.close();
        return products;
    }

    public static boolean addProductToStore(Store store, Product product) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("ADD_PRODUCT", store, product));
        out.flush();
        Object response = in.readObject();
        if (response.equals("Product added")){
            return true;
        }
        return false;
    }

    public static void reactivateProduct(Store store, Product product) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("REACTIVATE_PRODUCT", store, product));
        out.flush();
        Object response = in.readObject();
        Log.d("REACTIVATION", (String) response);
    }

    public static Map<String,Integer> sppStore(Store store) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("PRODUCT_SALES", store.getStoreName()));
        out.flush();
        Map<String, Integer> results = (Map<String, Integer>) in.readObject();
        return results;
    }

    public static Map<String,Integer> sppCategory(String prodCategory) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("PRODUCT_CATEGORY_SALES", prodCategory));
        out.flush();
        Map<String, Integer> results = (Map<String, Integer>) in.readObject();
        return results;
    }

    public static Map<String,Integer> spsCategory(String shopCategory) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("SHOP_CATEGORY_SALES", shopCategory));
        out.flush();
        Map<String, Integer> results = (Map<String, Integer>) in.readObject();
        return results;
    }

    public static List<String> getAllStoreCategories() throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(new WorkerFunctions("GET_ALL_STORE_CATEGORIES"));
        out.flush();

        Object response = in.readObject();
        List<String> categories = new ArrayList<>();
        if (response instanceof List) {
            categories = (List<String>) response;
        }

        out.close();
        in.close();
        socket.close();
        return categories;
    }

    public static List<String> getAllProductCategories() throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(new WorkerFunctions("GET_ALL_PRODUCT_CATEGORIES"));
        out.flush();

        Object response = in.readObject();
        List<String> categories = new ArrayList<>();
        if (response instanceof List) {
            categories = (List<String>) response;
        }

        out.close();
        in.close();
        socket.close();
        return categories;
    }

    public static boolean modifyAvailability(Store store, Product product, int quantity) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("MODIFY_STOCK",store,product,quantity));
        Object response = in.readObject();
        if(response instanceof Store){
            System.out.println("Server response: " + ((Store) response).getStoreName());
            Log.d("stock response", (String) response);
        }
        out.close();
        in.close();
        socket.close();
        return true;
    }


    public static List<Store> showAllStores() throws IOException, ClassNotFoundException {
        List<Store> stores = null;
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
        out.flush();
        Object response = inp.readObject();
        if(response instanceof ArrayList){
            stores = (List<Store>) response;
        }
        in.close();
        out.close();
        socket.close();
        return stores;
    }
}