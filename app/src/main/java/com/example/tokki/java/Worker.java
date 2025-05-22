package com.example.tokki.java;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Worker extends Thread{
    private int workerId;
    private List<Store> storeList = new ArrayList<>();
    private Runnable task = null;
    private Map<String, List<PendingPurchase>> pendingPurchases = new HashMap<>();
    private Queue<Runnable> pendingTasks = new LinkedList<>();
    private boolean isAlive = true;
    private boolean running = true;
    private Socket workerSocket;
    private ServerSocket workerServer;
    private int port;

    private static class PendingPurchase {
        String productName;
        int quantity;

        PendingPurchase(String productName, int quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }
    }


    public Worker(int port){
        this.port = port;
    }

    @Override
    public void run() {
        try {
            workerServer = new ServerSocket(port);
            System.out.println("Worker listening on port " + port);

            while (true) {
                Socket clientSocket = workerServer.accept();
                new ActionForWorkers(clientSocket, this).start();
            }
        } catch (IOException e) {
            if (!workerServer.isClosed()) {
                e.printStackTrace();
            }
        }
    }

    public int getPort(){
        return this.port;
    }

    public boolean ping() {
        return isAlive;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void receiveTask(Runnable task) {
        synchronized (pendingTasks) {
            pendingTasks.add(task);
            notify();
        }
    }

    public List<Store> getAllStores() {
        return new ArrayList<>(storeList);
    }

    public void clearStores() {
        synchronized (storeList) {
            storeList.clear();
        }
    }

    public void addStores(List<Store> stores) {
        synchronized(storeList){
            storeList.addAll(stores);
            storeList.forEach(Store::calculatePriceCategory);
        }
    }

    public void shutdown() {
        this.running = false;
        this.notifyAll();
    }

    public boolean addStore(Store store) {
        if(store!=null && !storeList.contains(store)) {
            synchronized (storeList) {
                storeList.add(store);
            }
            store.calculatePriceCategory();
            return true;
        }
        return false;
    }

    public boolean reserveProduct(Store store, Product product, Customer customer, int quantity) {
        Store currStore = getStore(store.getStoreName());
        synchronized(currStore){
            for(Product p : currStore.getProducts()){
                if(p.getProductName().equals(product.getProductName())){
                    if(p.getAvailableAmount() >= quantity && p.getAvailableAmount() > 0){
                        synchronized (p){
                            p.setAvailableAmount(p.getAvailableAmount() - quantity);
                        }
                        synchronized (pendingPurchases){
                            pendingPurchases
                                    .computeIfAbsent(store.getStoreName() + customer.getUsername(), k -> new ArrayList<>())
                                    .add(new PendingPurchase(product.getProductName(), quantity));
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean rollbackPurchase(String storeName, String username) {
        Store store = getStore(storeName);
        if(store == null || !pendingPurchases.containsKey(storeName+username)) {
            return false;
        }
        synchronized (store){
            List<PendingPurchase> pending = pendingPurchases.get(storeName+username);
            for (PendingPurchase pp : pending) {
                for (Product p : store.getProducts()) {
                    if (p.getProductName().equals(pp.productName)) {
                        synchronized (p){
                            p.setAvailableAmount(p.getAvailableAmount() + pp.quantity);
                            if (p.getAvailableAmount() > 0 && !p.isOnline()) {
                                p.setOnline(true);
                            }
                        }
                    }
                }
            }
            synchronized(pendingPurchases){
                pendingPurchases.remove(storeName+username);
            }
            return true;
        }
    }

    public void syncStore(Store primaryStore) {
        Store replicaStore = getStore(primaryStore.getStoreName());
        if (replicaStore == null) return;

        synchronized (replicaStore) {
            for (Product primaryProduct : primaryStore.getProducts()) {
                Product replicaProduct = replicaStore.getProduct(primaryProduct.getProductName());
                if (replicaProduct != null) {
                    synchronized (replicaProduct){
                        replicaProduct.setAvailableAmount(primaryProduct.getAvailableAmount());
                        replicaProduct.setTotalSales(primaryProduct.getTotalSales());
                        replicaProduct.setOnline(primaryProduct.isOnline());
                    }
                }
            }

            replicaStore.setStars(primaryStore.getStars());
            replicaStore.calculatePriceCategory();

            Worker primaryWorker = Master.getWorkerForStore(primaryStore.getStoreName(), true);
            if (primaryWorker != null) {
                Map<String, List<PendingPurchase>> primaryPending =
                        primaryWorker.getPendingPurchasesForStore(primaryStore.getStoreName());
                synchronized(pendingPurchases){
                    pendingPurchases.keySet().removeIf(key -> key.startsWith(primaryStore.getStoreName()));
                    pendingPurchases.putAll(primaryPending);
                }
            }
        }
    }


    public static int hashToWorker(String storeName, int numOfWorkers) {
        return Math.abs(storeName.hashCode()) % numOfWorkers;
    }

    public static List<Integer> getWorkerIndicesForStore(String storeName, int numOfWorkers) {
        int mainIndex = Math.abs(storeName.hashCode()) % numOfWorkers;
        int replicaIndex = (mainIndex + 1) % numOfWorkers;
        return Arrays.asList(mainIndex, replicaIndex);
    }

    public Map<String, List<PendingPurchase>> getPendingPurchasesForStore(String storeName) {
        return pendingPurchases.entrySet().stream()
                .filter(e -> e.getKey().startsWith(storeName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public boolean completePurchase(String storeName, String username) {
        Store store = getStore(storeName);
        if(store == null || !pendingPurchases.containsKey(storeName+username)) {
            return false;
        }
        synchronized (store) {
            List<PendingPurchase> pending = pendingPurchases.get(storeName+username);
            for (PendingPurchase pp : pending) {
                for (Product p : store.getProducts()) {
                    if (p.getProductName().equals(pp.productName)) {
                        synchronized (p){
                            p.addSales(pp.quantity);
                            if (p.getAvailableAmount() == 0) {
                                p.setOnline(false);
                            }
                        }
                    }
                }
            }
            synchronized(pendingPurchases){
                pendingPurchases.remove(storeName+username);
            }
            return true;
        }
    }

    public void modifyStock(Store store, Product product, int quantity) {
        for(Store s: storeList){
            if(s.equals(store)){
                synchronized (store) {
                    for (Product p : s.getProducts()) {
                        if (p.equals(product)) {
                            synchronized (product) {
                                p.setAvailableAmount(quantity);
                            }
                        }
                    }
                }
            }
        }
    }


    public boolean addProduct(Store store, Product product) {
        for(Store s : storeList){
            if(s.equals(store)){
                synchronized (s){
                    for(Product p : s.getProducts()){
                        if(p.getProductName().equals(product.getProductName())){
                            synchronized (product){
                                if(!p.isOnline()){
                                    p.setOnline(true);
                                }
                            }
                            s.calculatePriceCategory();
                            return false;
                        }
                    }
                    s.getProducts().add(product);
                    return true;
                }
            }
        }
        return false;
    }

    public String reactivateProduct(Store store, Product product) {
        for (Product p : store.getProducts()) {
            if (p.getProductName().equals(product.getProductName())) {
                synchronized(p){
                    p.setOnline(true);
                    p.setAvailableAmount(product.getAvailableAmount());
                }
            }
        }
        return "Reactivated product " + product.getProductName() + " from " + store.getStoreName();
    }

    public boolean removeProduct(Store store, Product product) {
        for(Store s : storeList){
            if(s.equals(store)){
                synchronized (s){
                    for(Product p: s.getProducts()){
                        if(p.getProductName().equals(product.getProductName())){
                            synchronized (p){
                                p.setOnline(false);
                            }
                            s.calculatePriceCategory();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public List<Product> getOfflineProducts(Store store) {
        Store localStore = getStore(store.getStoreName());
        if (localStore == null) return Collections.emptyList();

        synchronized (localStore){
            return localStore.getProducts().stream()
                    .filter(p -> !p.isOnline())
                    .collect(Collectors.toList());
        }
    }

    public List<Product> getOnlineProducts(Store store) {
        Store localStore = getStore(store.getStoreName());
        if (localStore == null) return Collections.emptyList();

        synchronized (localStore){
            return localStore.getProducts().stream()
                    .filter(p -> p.isOnline())
                    .collect(Collectors.toList());
        }
    }
    public Store getStore(String storeName) {
        for(Store store : storeList){
            if(store.getStoreName().equals(storeName)){
                return store;
            }
        }
        return null;
    }

    public List<Store> mapFilterStores(String category, double minRate, double maxRate, String priceCat) {
        return storeList.stream()
                .filter(store -> matchesFilter(store, category, minRate, maxRate, priceCat))
                .collect(Collectors.toList());
    }

    public boolean matchesFilter(Store store, String category, double minRate, double maxRate, String priceCat) {
        boolean result = true;
        Log.d("filter final", "category in filter " + category);
        if (category != null && !store.getFoodCategory().equalsIgnoreCase(category)) {
            return false;
        }
        if (store.getStars() < minRate || store.getStars() > maxRate) {
            return false;
        }
        if (priceCat != null && !store.getPriceCategory().equalsIgnoreCase(priceCat)) {
            return false;
        }
        return result;
    }

    public void rateStore(Store store, int rating){
        for(Store s : storeList){
            if(s.getStoreName().equals(store.getStoreName())){
                synchronized (s){
                    s.applyRating(rating);
                    return;
                }
            }
        }
    }

    public List<Store> showAllStores(){
        return new ArrayList<>(storeList);
    }

    public List<Store> showStores(Customer customer){
        List<Store> stores = new ArrayList<>();
        for(Store store : storeList){
            if(isWithInRange(store, customer)){
                stores.add(store);
            }
        }
        return stores;
    }

    public Map<String, Integer> mapProductSales(String storeName) {
        Map<String, Integer> results = new HashMap<>();
        Store store = getStore(storeName);
        if (store != null) {
            for (Product product : store.getProducts()) {
                results.put(product.getProductName(), product.getTotalSales());
            }
        }
        return results;
    }

    public Map<String, Integer> mapProductCategorySales(String productCategory) {
        Map<String, Integer> results = new HashMap<>();
        for (Store store : storeList) {
            int categorySales = 0;
            for (Product product : store.getProducts()) {
                if (product.getProductType().equals(productCategory)) {
                    categorySales += product.getTotalSales();
                }
            }
            if (categorySales > 0) {
                results.put(store.getStoreName(), categorySales);
            }
        }
        return results;
    }

    public Map<String, Integer> mapShopCategorySales(String shopCategory) {
        Map<String, Integer> results = new HashMap<>();
        for (Store store : storeList) {
            if (store.getFoodCategory().equals(shopCategory)) {
                for (Product product : store.getProducts()) {
                    String productName = product.getProductName();
                    int sales = product.getTotalSales();
                    if (results.containsKey(productName)) {
                        results.put(productName, results.get(productName) + sales);
                    } else {
                        results.put(productName, sales);
                    }
                }
            }
        }
        return results;
    }

    public boolean isWithInRange(Store store, Customer customer) {
        double storeLat = store.getLatitude();
        double storeLong = store.getLongitude();
        double customerLat = customer.getLatitude();
        double customerLong = customer.getLongitude();

        final double R = 6371.0;

        double lat1 = Math.toRadians(customerLat);
        double lon1 = Math.toRadians(customerLong);
        double lat2 = Math.toRadians(storeLat);
        double lon2 = Math.toRadians(storeLong);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance <= 5.0;
    }

    public boolean shouldIncludeStore(Worker worker, List<Worker> workers, String storeName) {
        List<Integer> indices = getWorkerIndicesForStore(storeName, workers.size());
        Worker primary = workers.get(indices.get(0));

        return worker.getWorkerId() == primary.getWorkerId() ||
                (!primary.isAlive() && worker.getWorkerId() == indices.get(1));
    }

}