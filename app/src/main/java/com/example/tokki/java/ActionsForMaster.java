package com.example.tokki.java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;


public class ActionsForMaster extends Thread {
    private Socket masterSocket;
    private Master master;
    private static int nums = 1;
    public ActionsForMaster(Socket masterSocket, Master master) {
        this.masterSocket = masterSocket;
        this.master = master;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(masterSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream())) {

            WorkerFunctions request = (WorkerFunctions) in.readObject();
            processRequest(request, out);
            out.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        } finally {
            try {
                masterSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    public void processRequest(WorkerFunctions request, ObjectOutputStream out) throws IOException {
        String operation = request.getOperation();

        try {
            switch (operation) {
                case "REGISTER":
                    handleAddWorker();
                    break;
                case "ADD_STORE":
                    handleAddStore(request, out);
                    break;

                case "GET_OFFLINE_PRODUCTS":
                    handleGetOfflineProducts(request, out);
                    break;

                case "ADD_PRODUCT":
                    handleAddProduct(request, out);
                    break;

                case "REMOVE_PRODUCT":
                    handleRemoveProduct(request, out);
                    break;

                case "MODIFY_STOCK":
                    handleModifyStock(request, out);
                    break;

                case "REACTIVATE_PRODUCT":
                    handleReactivateProduct(request, out);
                    break;

                case "APPLY_RATING":
                    handleApplyRating(request, out);
                    break;

                case "RESERVE_PRODUCT":
                    handleReserveProduct(request, out);
                    break;

                case "COMPLETE_PURCHASE":
                    handleCompletePurchase(request, out);
                    break;

                case "ROLLBACK_PURCHASE":
                    handleRollbackPurchase(request, out);
                    break;

                case "SHOW_STORES":
                    handleShowStores(request, out);
                    break;

                case "SHOW_ALL_STORES":
                    handleShowAllStores(request, out);
                    break;

                case "FILTER_STORES":
                    handleFilterStores(request, out);
                    break;

                case "PRODUCT_SALES":
                    handleProductSales(request, out);
                    break;

                case "PRODUCT_CATEGORY_SALES":
                    handleProductCategorySales(request, out);
                    break;

                case "SHOP_CATEGORY_SALES":
                    handleShopCategorySales(request, out);
                    break;

                default:
                    out.writeObject("Unsupported operation");
            }
        } catch (Exception e) {
            out.writeObject("Error processing request: " + e.getMessage());
        }
    }

    private void handleAddWorker(){
        Worker worker = new Worker(8080 + (++nums));
        Master.getWorkers().add(worker);
        worker.start();
    }

    private void handleAddStore(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        Object response = forwardToWorker(primaryWorker, request);
        out.writeObject(response);

        if (response instanceof Store) {
            forwardToWorker(replicaWorker,
                    new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        }
    }

    private void handleGetOfflineProducts(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store targetStore = (Store) request.getObject();
        List<Integer> assign = Master.getWorkerIndicesForStore(targetStore.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));

        if (master.isAlive(primaryWorker)) {
            Object products = forwardToWorker(primaryWorker, request);
            out.writeObject(products);
        } else {
            Worker replicaWorker = master.getWorkers().get(assign.get(1));
            Object products = forwardToWorker(replicaWorker, request);
            out.writeObject(products);
        }
    }

    private void handleAddProduct(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject();
        Product product = (Product) request.getObject2();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        Object response = forwardToWorker(primaryWorker, request);
        out.writeObject(response);

        if (response instanceof String && ((String) response).contains("success")) {
            forwardToWorker(replicaWorker,
                    new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        }
    }

    private void handleRemoveProduct(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject();
        Product product = (Product) request.getObject2();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        Object response = forwardToWorker(primaryWorker, request);
        out.writeObject(response);

        if (response instanceof String && ((String) response).contains("Removed")) {
            forwardToWorker(replicaWorker,
                    new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        }
    }

    private void handleModifyStock(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject();
        Product product = (Product) request.getObject2();
        int quantity = request.getNum();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        forwardToWorker(primaryWorker, request);
        forwardToWorker(replicaWorker,
                new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        out.writeObject("Stock updated");
    }

    private void handleReactivateProduct(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject();
        Product product = (Product) request.getObject2();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        Object response = forwardToWorker(primaryWorker, request);
        out.writeObject(response);

        if (response instanceof String && ((String) response).contains("Reactivated")) {
            forwardToWorker(replicaWorker,
                    new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        }
    }

    private void handleApplyRating(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject();
        int rating = request.getNum();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        forwardToWorker(primaryWorker, request);
        forwardToWorker(replicaWorker,
                new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        out.writeObject("Rating applied");
    }

    private void handleReserveProduct(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject2();
        Product product = (Product) request.getObject();
        Customer customer = (Customer) request.getObject3();
        int quantity = request.getNum();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        Object response = forwardToWorker(primaryWorker, request);
        out.writeObject(response);

        if (response instanceof Customer.ProductOrder) {
            forwardToWorker(replicaWorker,
                    new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        }
    }

    private void handleCompletePurchase(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject();
        Customer customer = (Customer) request.getObject2();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        Object response = forwardToWorker(primaryWorker, request);
        out.writeObject(response);

        if (response instanceof String && ((String) response).contains("success")) {
            forwardToWorker(replicaWorker,
                    new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        }
    }

    private void handleRollbackPurchase(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Store store = (Store) request.getObject();
        Customer customer = (Customer) request.getObject2();
        List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName());
        Worker primaryWorker = master.getWorkers().get(assign.get(0));
        Worker replicaWorker = master.getWorkers().get(assign.get(1));

        Object response = forwardToWorker(primaryWorker, request);
        out.writeObject(response);

        if (response instanceof String && ((String) response).contains("success")) {
            forwardToWorker(replicaWorker,
                    new WorkerFunctions("SYNC_STORE", primaryWorker.getStore(store.getStoreName())));
        }
    }

    private void handleShowStores(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        Customer customer = (Customer) request.getObject();
        List<Store> stores = new ArrayList<>();

        for (Worker worker : master.getWorkers()) {
            Object response = forwardToWorker(worker,
                    new WorkerFunctions("SHOW_STORES", customer));
            if (response instanceof List) {
                stores.addAll((List<Store>) response);
            }
        }

        out.writeObject(stores);
    }

    private void handleShowAllStores(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        List<Store> stores = new ArrayList<>();

        for (Worker worker : master.getWorkers()) {
            Object response = forwardToWorker(worker,
                    new WorkerFunctions("SHOW_ALL_STORES"));
            if (response instanceof List) {
                stores.addAll((List<Store>) response);
            }
        }

        out.writeObject(stores);
    }

    private void handleFilterStores(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        String foodCategory = request.getName();
        double lowerStars = request.getDouble1();
        double upperStars = request.getDouble2();
        String priceCategory = request.getName2();
        String requestId = generateRequestId();

        master.storeClientConnection(requestId, out);

        for (Worker worker : master.getWorkers()) {
            forwardToWorker(worker,
                    new WorkerFunctions("FILTER_STORES", foodCategory, lowerStars, upperStars, priceCategory, requestId));
        }
    }

    private void handleProductSales(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        String storeName = request.getName();
        String requestId = generateRequestId();

        master.storeClientConnection(requestId, out);

        for (Worker worker : master.getWorkers()) {
            forwardToWorker(worker,
                    new WorkerFunctions("PRODUCT_SALES", requestId, storeName));
        }

    }

    private void handleProductCategorySales(WorkerFunctions request, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        String productCategory = request.getName();
        String requestId = generateRequestId();

        master.storeClientConnection(requestId, out);

        for (Worker worker : master.getWorkers()) {
            forwardToWorker(worker,
                    new WorkerFunctions("PRODUCT_CATEGORY_SALES", requestId, productCategory));
        }
    }

    private void handleShopCategorySales(WorkerFunctions request, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        String shopCategory = request.getName();
        String requestId = generateRequestId();

        master.storeClientConnection(requestId, out);

        for (Worker worker : master.getWorkers()) {
            forwardToWorker(worker,
                    new WorkerFunctions("SHOP_CATEGORY_SALES", requestId, shopCategory));
        }
    }

    private Object forwardToWorker(Worker worker, WorkerFunctions request)
            throws IOException, ClassNotFoundException {
        try (Socket workerSocket = new Socket("127.0.0.1", worker.getPort());
             ObjectOutputStream workerOut = new ObjectOutputStream(workerSocket.getOutputStream());
             ObjectInputStream workerIn = new ObjectInputStream(workerSocket.getInputStream())) {

            workerOut.writeObject(request);
            workerOut.flush();
            return workerIn.readObject();
        }
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    private Worker findWorkerForStore(String storeName) {
        List<Worker> workers = master.getWorkers();
        int index = Master.hashToNode(storeName, workers.size());
        return workers.get(index);
    }
}