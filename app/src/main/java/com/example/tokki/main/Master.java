package com.example.tokki.main;
import java.io.*;
import java.net.*;
import java.util.*;

public class Master {
    ServerSocket serverSocket;
    Socket socket;
    private static List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
    private Properties config;
    private static int nodesSize;
    private static int portNums = 1;
    private Map<String, ObjectOutputStream> clientConnections = new HashMap<>();

    public Master() {
        this.config = new Properties();

        try (InputStream input = new FileInputStream("src/main/config.properties")) {
            config.load(input);
            int nodeCount = Integer.parseInt(config.getProperty("nodeCount"));
            nodesSize = nodeCount;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Master(int nodeCount, int workersPerNode) {
        nodesSize = nodeCount;
    }


    public static List<Worker> getWorkers(){
        return workers;
    }

    public int getPortNums(){
        return portNums;
    }

    public static void main(String[] args) {
        Master master;
        if (args.length > 0) {
            try {
                int nodeCount = Integer.parseInt(args[0]);
                int workersPerNode = args.length > 1 ? Integer.parseInt(args[1]) : 2;
                System.out.println("Starting master with " + nodeCount + " nodes and " + workersPerNode + " workers per node");
                master = new Master(nodeCount, workersPerNode);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out.println("Invalid arguments");
                master = new Master();
            }
        } else {
            System.out.println("Reading configuration from file");
            master = new Master();
            //registerWorker();
        }
        Reducer reducer = new Reducer(9090, master); // Use an appropriate port
        reducer.start();
        master.openServer();
    }


    void openServer() {
        new Thread(()-> {
            try {
                System.out.println("Opening server...");
                serverSocket = new ServerSocket(8080);
                while (true) {
                    socket = serverSocket.accept();
                    Thread t = new ActionsForMaster(socket, this);
                    t.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void storeClientConnection(String requestId, ObjectOutputStream out) {
        synchronized (clientConnections) {
            clientConnections.put(requestId, out);
        }
    }

    public void handleReducedResult(String requestId, Object results) {
        synchronized(clientConnections) {
            ObjectOutputStream out = clientConnections.remove(requestId);
            if (out != null) {
                try {
                    out.writeObject(results);
                    out.flush();
                } catch (IOException e) {
                    System.err.println("Error sending reduced results to client: " + e.getMessage());
                }
            }
        }
    }


    public static synchronized void rebalanceStores() {
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
            int workerIndex = Worker.hashToWorker(store.getStoreName(), numWorkers);
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

    public static void registerWorker() throws IOException {
        Socket masterSocket = new Socket("localhost", 8080);  // Connect to Master
        ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(masterSocket.getInputStream());
        out.writeObject(new WorkerFunctions("REGISTER"));
        rebalanceStores();
        out.flush();
    }


    public static int hashToNode(String storeName, int numOfNodes) {
        return Math.abs(storeName.hashCode()) % numOfNodes;
    }

    public static List<Integer> getWorkerIndicesForStore(String storeName) {
        System.out.println("Master's workers " + workers.size());
        int mainIndex = Math.abs(storeName.hashCode()) % workers.size();
        int replicaIndex = (mainIndex + 1) % workers.size();
        return Arrays.asList(mainIndex, replicaIndex);
    }

    public static Worker getWorkerForStore(String storeName, boolean getPrimary) {
        List<Integer> workerIndices = Worker.getWorkerIndicesForStore(storeName, workers.size());
        int workerIndex = getPrimary ? workerIndices.get(0) : workerIndices.get(1);

        if (workerIndex >= 0 && workerIndex < workers.size()) {
            return workers.get(workerIndex);
        }
        return null;
    }

    public boolean isAlive(Worker worker) {
        final boolean[] result = {false};
        Thread t = new Thread(() -> {
            result[0] = worker.isAlive();
        });
        t.start();
        try {
            t.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return !t.isAlive() && result[0];
    }
}