package com.example.tokki.java;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer implements Serializable {
    private String username,password;
    private double longitude,latitude;
    private static final long serialVersionUID = 1254840259994782788L;
    public static class ProductOrder implements Serializable {
        /*
            Bohthitikh klash gia na parakolouthei to kalathi mias paraggelias
            kai na to emfanizei sto telos
         */
        final String productName;
        final int quantity;

        ProductOrder(String productName, int quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "Product: " + productName + " Quantity: " + quantity;
        }
    }

    public Customer(String username, String password, double latitude, double longitude){
        this.username = username;
        this.password = password;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /*
    public void showNearbyStores(){

            Deixnei ta katasthmata se apostash 5km apo ton pelath

        try{
            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_STORES", this));
            out.flush();
            Object response = in.readObject();
            if(response instanceof ArrayList){
                System.out.println("Server response: ");
                for(Store store : (ArrayList<Store>)response){
                    System.out.println(store.getStoreName());
                }
            }
            out.close();
            in.close();
            socket.close();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    */

    public List<Store> showNearbyStores(){
        /*
            Deixnei ta katasthmata se apostash 5km apo ton pelath
         */
        try{
            List<Store> stores = null;
            Socket socket = new Socket("192.168.2.9", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_STORES", this));
            out.flush();
            Object response = in.readObject();
            if(response instanceof ArrayList){
                stores = (List<Store>) response;
            }
            out.close();
            in.close();
            socket.close();
            return stores;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public void buyProducts(Scanner input){
        /*
            Dhmiourgia paraggelias apo katasthma kai agora
         */
        try{
            Socket socket = new Socket("192.168.2.9", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_STORES", this));
            out.flush();
            Object response = in.readObject();
            if(response instanceof ArrayList){
                System.out.println("Server response: ");
                int counter = 0;
                System.out.println("Choose store");
                for(Store store : (ArrayList<Store>)response){
                    System.out.println(++counter +". "+ store.getStoreName());
                }
                int choice = input.nextInt();
                if(choice >= 1 && choice <= ((ArrayList<?>) response).size()){
                    Store store = ((ArrayList<Store>) response).get(choice-1);
                    List<ProductOrder> cart = new ArrayList<>(); //Lista pou krataei apothikeumeno to kalathi ths paraggelias
                    boolean purchasing = true;
                    while(purchasing){
                        if(cart.isEmpty()){     //se kathe epanalhpsh tiponei to torino kalathi paraggelias
                            System.out.println("No products exist in the cart yet");
                        }else{
                            System.out.println("Current cart: ");
                            for(ProductOrder order : cart){
                                System.out.println(order.toString());
                            }
                        }
                        System.out.println("Choose product");
                        ArrayList<Product> products = new ArrayList<>();
                        for(Product product : store.getProducts()){    //Apothikeuei ta online proionta mono
                            if (product.isOnline()) {
                                products.add(product);
                            }
                        }
                        counter = 1;
                        for(Product product : products){    //Epilogh me bash ta online proionta
                            System.out.println(counter + ". " + product.getProductName());
                            counter++;
                        }
                        System.out.println(counter + ". " + "Quit order");
                        System.out.println("0. Complete purchase");
                        choice = input.nextInt();
                        if(choice >= 1 && choice <= products.size()){
                            System.out.println("Choose quantity");
                            socket = new Socket("localhost", 8080);
                            out = new ObjectOutputStream(socket.getOutputStream());
                            in = new ObjectInputStream(socket.getInputStream());
                            int quantity = input.nextInt();
                            out.writeObject(new WorkerFunctions("RESERVE_PRODUCT", products.get(choice-1),store,this, quantity));  //Desmeuei ena proion prosorina
                            out.flush();
                            Object response2 = in.readObject();
                            if(response2 instanceof ProductOrder) {
                                System.out.println("Product reserved successfully");
                                cart.add((ProductOrder) response2);  //Enhmerwsh kalathiou
                            }else{
                                System.out.println(response2);
                            }
                        }else if(choice == 0){
                            /*
                                Oristikopoiei thn paraggelia kai tiponei to kalathi me ta proionta
                             */
                            socket = new Socket("192.168.2.9", 8080);
                            out = new ObjectOutputStream(socket.getOutputStream());
                            in = new ObjectInputStream(socket.getInputStream());
                            out.writeObject(new WorkerFunctions("COMPLETE_PURCHASE", store, this));
                            out.flush();
                            response = (String) in.readObject();
                            if(response.equals("Purchase successful")){
                                System.out.println("Purchase completed");
                                for(ProductOrder order : cart){
                                    System.out.println(order.toString());
                                }
                            }else{
                                System.out.println("Purchase failed");
                            }
                            purchasing = false;
                        }else{
                            /*
                                Akironei thn paraggelia kai epitrefei to katasthma sthn katastash pou eixe prin thn
                                enarksh ths
                             */
                            socket = new Socket("192.168.2.9", 8080);
                            out = new ObjectOutputStream(socket.getOutputStream());
                            in = new ObjectInputStream(socket.getInputStream());
                            out.writeObject(new WorkerFunctions("ROLLBACK_PURCHASE", store, this));
                            out.flush();
                            response = (String) in.readObject();
                            System.out.println("Order cancelled");
                            purchasing = false;
                        }
                    }
                    out.close();
                    in.close();
                    socket.close();
                }
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean reserveProduct(Product product, Store store, int quantity) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("192.168.2.9", 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("RESERVE_PRODUCT", product,store,this, quantity));  //Desmeuei ena proion prosorina
        out.flush();
        Object response = in.readObject();
        if(response instanceof ProductOrder) {
            System.out.println("Product reserved successfully");
            out.close();
            in.close();
            socket.close();
            return true;
        }else{
            System.out.println(response);
        }
        out.close();
        in.close();
        socket.close();
        return false;
    }

    public boolean rollbackPurchase(Store store) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("192.168.2.9", 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("ROLLBACK_PURCHASE", store, this));
        out.flush();
        Object response = (String) in.readObject();
        out.close();
        in.close();
        socket.close();
        System.out.println("Order cancelled");
        return true;
    }

    public boolean completePurchase(Store store) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("192.168.2.9", 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("COMPLETE_PURCHASE", store, this));
        out.flush();
        Object response = (String) in.readObject();
        if(response.equals("Purchase successful")){
            System.out.println("Purchase completed");
            return true;
        }else{
            System.out.println("Purchase failed");
            return false;
        }
    }


    public void filterStores(Scanner in){
            /*
            Epilogh apo diathesima filtra kai provolh twn katasthmatwn pou antistoixoun se auta

             */

        try {
            System.out.println("Choose Food Category (leave empty for any):");
            String category = in.nextLine();
            System.out.println("Minimum Rating (1-5)");
            double minRate = in.nextDouble();
            System.out.println("Maximum Rating (1-5)");
            double maxRate = in.nextDouble();
            in.nextLine();
            System.out.println("Price Category (leave empty for any):");
            String priceCat = in.nextLine();

            Socket socket = new Socket("192.168.2.9", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new WorkerFunctions("FILTER_STORES",
                    category.isEmpty() ? null : category,
                    minRate, maxRate,
                    priceCat.isEmpty() ? null : priceCat));
            out.flush();

            List<Store> results = (List<Store>) inp.readObject();
            System.out.println("Found " + results.size() + " results");
            results.forEach(store ->
                System.out.println(store.getStoreName() + "(" + store.getFoodCategory() + ") - Rating: " + store.getStars()
                        + " - Price: " + store.getPriceCategory()));
            out.close();
            inp.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<Store> filterStores(String category, double minRate, double maxRate, String priceCat){
        /*
        Epilogh apo diathesima filtra kai provolh twn katasthmatwn pou antistoixoun se auta
         */
        List<Store> results = null;
        try {
            Socket socket = new Socket("192.168.2.9", 8080);
            Log.d("Category", "Category in customer: " + category);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("FILTER_STORES",
                    category.isEmpty() ? null : category,
                    minRate, maxRate,
                    priceCat.isEmpty() ? null : priceCat));
            out.flush();

            results = (List<Store>) in.readObject();
            System.out.println("Found " + results.size() + " results");
            //results.forEach(store ->
              //      System.out.println(store.getStoreName() + "(" + store.getFoodCategory() + ") - Rating: " + store.getStars()
                //            + " - Price: " + store.getPriceCategory()));
            out.close();
            in.close();
            socket.close();
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public void rateStore(Scanner in){
        /*
            Bathmologhsh katasthmatos
         */
        try{
            Socket socket = new Socket("192.168.2.9", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
            out.flush();
            Object response = inp.readObject();
            if(response instanceof ArrayList){
                System.out.println("Server response: ");
                System.out.println("Choose store to rate:");
                int counter = 0;
                for(Store store : (ArrayList<Store>)response){
                    System.out.println(++counter + " " + store.getStoreName());
                }
                System.out.println("0. Exit");
                int choice = in.nextInt();
                if(choice >= 1 && choice <= ((ArrayList<?>) response).size()){
                    Store store = ((ArrayList<Store>) response).get(choice-1);
                    System.out.println("Original rating: " + String.format("%.1f",store.getStars()));
                    System.out.println("Give rating: ");
                    int rating = in.nextInt();
                    if(rating >=1 && rating <= 5){
                        socket = new Socket("localhost", 8080);
                        out = new ObjectOutputStream(socket.getOutputStream());
                        inp = new ObjectInputStream(socket.getInputStream());
                        out.writeObject(new WorkerFunctions("APPLY_RATING",store, rating));
                        out.flush();
                        Object response2 = inp.readObject();
                        if(response2 instanceof String){
                            System.out.println(response2);
                        }
                    }else{
                        System.out.println("Rating out of bounds");
                    }
                }else if(choice != 0){
                    System.out.println("Invalid input");
                }
                out.close();
                inp.close();
                socket.close();
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}