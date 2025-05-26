package com.example.tokki.java;

import java.io.*;
import java.net.*;
import java.util.*;

public class ActionForWorkers extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    private List<Worker> workers;
    private Master master;
    private Worker worker;
    private Socket workerSocket;

    public ActionForWorkers(Socket connection, List<Worker> workers, Master master) {
        this.workers = workers;
        this.master = master;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ActionForWorkers(Socket workerSocket, Worker worker) {
        this.workerSocket = workerSocket;
        this.worker = worker;
        try {
            out = new ObjectOutputStream(workerSocket.getOutputStream());
            in = new ObjectInputStream(workerSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            try {
                while (true) {
                    WorkerFunctions request = (WorkerFunctions) in.readObject();
                    processRequest(request);
                }
            } catch (EOFException e) {
                System.out.println("Client disconnected.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void rebalanceStores() {
        List<Store> allStores = new ArrayList<>();
        for (Worker worker : workers) {
            allStores.addAll(worker.getAllStores());
        }

        for (Worker worker : workers) {
            worker.clearStores();
        }

        int numWorkers = workers.size();
        Map<Integer, List<Store>> storeAssignment = new HashMap<>();

        for (Store store : allStores) {
            int workerIndex = Master.hashToNode(store.getStoreName(), numWorkers);
            storeAssignment.computeIfAbsent(workerIndex, k -> new ArrayList<>()).add(store);
        }

        for (Map.Entry<Integer, List<Store>> entry : storeAssignment.entrySet()) {
            int workerIndex = entry.getKey();
            if (workerIndex < workers.size()) {
                workers.get(workerIndex).addStores(entry.getValue());
            }
        }
        System.out.println("Stores rebalanced across " + numWorkers + " workers");
    }

    public static List<Integer> getWorkerIndicesForStore(String storeName, int numOfWorkers) {
        if (numOfWorkers == 0) {
            throw new IllegalStateException("No workers available");
        }
        int mainIndex = Math.abs(storeName.hashCode()) % numOfWorkers;
        int replicaIndex = (mainIndex + 1) % numOfWorkers;
        return Arrays.asList(mainIndex, replicaIndex);
    }

    private void processRequest(WorkerFunctions request) throws IOException {
        String operation = request.getOperation();

        try {
            switch (operation) {
                case "ADD_STORE":
                    handleAddStore(request);
                    break;

                case "SYNC_STORE":
                    handleSyncStore(request);
                    break;

                case "ADD_PRODUCT":
                    handleAddProduct(request);
                    break;
                case "GET_ALL_PRODUCTS":
                    handleGetAllProducts(request);
                    break;
                case "GET_OFFLINE_PRODUCTS":
                    handleGetOfflineProducts(request);
                    break;
                case "GET_ONLINE_PRODUCTS":
                    handleGetOnlineProducts(request);
                    break;
                case "MODIFY_STOCK":
                    handleModifyStock(request);
                    break;

                case "HEARTBEAT":
                    handleHeartbeat();
                    break;

                case "REMOVE_PRODUCT":
                    handleRemoveProduct(request);
                    break;

                case "REACTIVATE_PRODUCT":
                    handleReactivateProduct(request);
                    break;

                case "APPLY_RATING":
                    handleApplyRating(request);
                    break;

                case "RESERVE_PRODUCT":
                    handleReserveProduct(request);
                    break;

                case "COMPLETE_PURCHASE":
                    handleCompletePurchase(request);
                    break;

                case "ROLLBACK_PURCHASE":
                    handleRollbackPurchase(request);
                    break;

                case "SHOW_STORES":
                    handleShowStores(request);
                    break;

                case "SHOW_ALL_STORES":
                    handleShowAllStores();
                    break;

                case "FILTER_STORES":
                    handleFilterStores(request);
                    break;

                case "PRODUCT_SALES":
                    handleProductSales(request);
                    break;

                case "PRODUCT_CATEGORY_SALES":
                    handleProductCategorySales(request);
                    break;

                case "SHOP_CATEGORY_SALES":
                    handleShopCategorySales(request);
                    break;

                default:
                    out.writeObject("Unsupported operation");
            }
        } catch (Exception e) {
            out.writeObject("Error processing request: " + e.getMessage());
        }
        out.flush();
    }

    private void handleAddStore(WorkerFunctions request) throws IOException {
        Store store = (Store) request.getObject();
        boolean added = worker.addStore(store);
        out.writeObject(added ? store : "Store already exists");
    }

    private void handleSyncStore(WorkerFunctions request) throws IOException {
        Store storeToSync = (Store) request.getObject();
        worker.syncStore(storeToSync);
        out.writeObject("Sync completed");
    }

    private void handleAddProduct(WorkerFunctions request) throws IOException {
        Store targetStore = (Store) request.getObject();
        Product product = (Product) request.getObject2();
        boolean productAdded = worker.addProduct(targetStore, product);
        out.writeObject(productAdded ? "Product added" : "Product exists");
    }

    private void handleGetAllProducts(WorkerFunctions request) throws IOException {
        Store storeForProducts = (Store) request.getObject();
        List<Product> offlineProducts = worker.getAllProducts(storeForProducts);
        out.writeObject(offlineProducts);
    }
    private void handleGetOfflineProducts(WorkerFunctions request) throws IOException {
        Store storeForProducts = (Store) request.getObject();
        List<Product> offlineProducts = worker.getOfflineProducts(storeForProducts);
        out.writeObject(offlineProducts);
    }

    private void handleGetOnlineProducts(WorkerFunctions request) throws IOException{
        Store storeForProducts = (Store) request.getObject();
        List<Product> offlineProducts = worker.getOnlineProducts(storeForProducts);
        out.writeObject(offlineProducts);
    }
    private void handleModifyStock(WorkerFunctions request) throws IOException {
        Store stockStore = (Store) request.getObject();
        Product stockProduct = (Product) request.getObject2();
        int quantity = request.getNum();
        worker.modifyStock(stockStore, stockProduct, quantity);
        out.writeObject("Stock updated");
    }

    private void handleHeartbeat() throws IOException {
        out.writeObject("ALIVE");
    }

    private void handleRemoveProduct(WorkerFunctions request) throws IOException {
        Store store = (Store) request.getObject();
        Product product = (Product) request.getObject2();
        boolean removed = worker.removeProduct(store, product);
        out.writeObject(removed ? "Product removed" : "Product not found");
    }

    private void handleReactivateProduct(WorkerFunctions request) throws IOException {
        Store store = (Store) request.getObject();
        Product product = (Product) request.getObject2();
        String result = worker.reactivateProduct(store, product);
        out.writeObject(result);
    }

    private void handleApplyRating(WorkerFunctions request) throws IOException {
        Store store = (Store) request.getObject();
        int rating = request.getNum();
        worker.rateStore(store, rating);
        out.writeObject("Rating applied");
    }

    private void handleReserveProduct(WorkerFunctions request) throws IOException {
        Store store = (Store) request.getObject2();
        Product product = (Product) request.getObject();
        Customer customer = (Customer) request.getObject3();
        int quantity = request.getNum();
        boolean reserved = worker.reserveProduct(store, product, customer, quantity);
        out.writeObject(reserved ? new Customer.ProductOrder(product.getProductName(), quantity) : "Reservation failed");
    }

    private void handleCompletePurchase(WorkerFunctions request) throws IOException {
        Store store = (Store) request.getObject();
        Customer customer = (Customer) request.getObject2();
        boolean completed = worker.completePurchase(store.getStoreName(), customer.getUsername());
        out.writeObject(completed ? "Purchase successful" : "Purchase failed");
    }

    private void handleRollbackPurchase(WorkerFunctions request) throws IOException {
        Store store = (Store) request.getObject();
        Customer customer = (Customer) request.getObject2();
        boolean reverted = worker.rollbackPurchase(store.getStoreName(), customer.getUsername());
        out.writeObject(reverted ? "Revert successful" : "Revert unsuccessful");
    }

    private void handleShowStores(WorkerFunctions request) throws IOException {
        Customer customer = (Customer) request.getObject();
        List<Store> stores = worker.showStores(customer);
        out.writeObject(stores);
    }

    private void handleShowAllStores() throws IOException {
        List<Store> stores = worker.showAllStores();
        out.writeObject(stores);
    }


    private void handleFilterStores(WorkerFunctions request) throws IOException {
        String foodCategory = request.getName();
        double lowerStars = request.getDouble1();
        double upperStars = request.getDouble2();
        String priceCategory = request.getName2();
        String id = request.getName3();
        List<Store> results = worker.mapFilterStores(foodCategory, lowerStars, upperStars, priceCategory);
        sendFilterToReducer("FILTER_STORES", id, results);
    }

    private void handleProductSales(WorkerFunctions request) throws IOException {
        String storeName = request.getName2();
        String id = request.getName();
        Map<String, Integer> results = worker.mapProductSales(storeName);
        System.out.println("Sending from worker");
        sendToReducer("PRODUCT_SALES", id, results);
    }

    private void handleProductCategorySales(WorkerFunctions request) throws IOException {
        String productCategory = request.getName2();
        String id = request.getName();
        Map<String, Integer> results = worker.mapProductCategorySales(productCategory);
        System.out.println("Sending from worker");
        sendToReducer("PRODUCT_CATEGORY_SALES", id, results);
    }

    private void handleShopCategorySales(WorkerFunctions request) throws IOException {
        String shopCategory = request.getName2();
        String id = request.getName();
        Map<String, Integer> results = worker.mapShopCategorySales(shopCategory);
        System.out.println("Sending from worker");
        sendToReducer("SHOP_CATEGORY_SALES", id, results);
    }

    private void sendToReducer(String operation, String key, Map<String, Integer> mappedResults) {
        try {
            Socket reducerSocket = new Socket("127.0.0.1", 9090);
            ObjectOutputStream out = new ObjectOutputStream(reducerSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(reducerSocket.getInputStream());
            out.writeObject(operation);
            out.writeObject(key);
            out.writeObject(mappedResults);
            out.flush();

            reducerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFilterToReducer(String operation, String key, List<Store> mappedResults) {
        try {
            Socket reducerSocket = new Socket("127.0.0.1", 9090);
            ObjectOutputStream out = new ObjectOutputStream(reducerSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(reducerSocket.getInputStream());
            out.writeObject(operation);
            out.writeObject(key);
            out.writeObject(mappedResults);
            out.flush();

            reducerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

