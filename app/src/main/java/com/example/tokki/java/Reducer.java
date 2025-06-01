package com.example.tokki.java;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Reducer extends Thread {
    private final int port;
    private ServerSocket serverSocket;
    private final Master master;
    private boolean running;

    private final Map<String, List<Map<String, Integer>>> pendingReductions = new HashMap<>();
    private final Map<String, List<List<Store>>> pendingRed = new HashMap<>();

    public Reducer(int port, Master master) {
        this.port = port;
        this.master = master;
        this.running = true;
    }

    @Override
    public void run() {
        /*
        ekkinhsh reducer server
         */
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Reducer started on port " + port);
            while (running) {
                Socket workerSocket = serverSocket.accept();
                new ReducerHandler(workerSocket, master, this).start();
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addPartialResult(String requestId, Map<String, Integer> mappedResults) throws IOException {
        /*
        prosthiki meros apo results enos request sto swsto id
         */
        synchronized (pendingReductions){
            pendingReductions.computeIfAbsent(requestId, k -> new ArrayList<>()).add(mappedResults);
        }

        if (pendingReductions.get(requestId).size() == master.getWorkers().size()) {
            /*
            efoson elabe apanthsh apo kathe worker kane to teliko reduce
             */
            Map<String, Integer> finalResult = reduceAllResults(pendingReductions.get(requestId));
            synchronized (pendingReductions){
                pendingReductions.remove(requestId);
            }
            sendToMaster(requestId, finalResult);
        }
    }

    private Map<String, Integer> reduceAllResults(List<Map<String, Integer>> partialResults) {
        Map<String, Integer> finalResult = new HashMap<>();
        for (Map<String, Integer> partial : partialResults) {
            partial.forEach((k, v) -> finalResult.merge(k, v, Integer::sum));
        }
        return finalResult;
    }

    private void sendToMaster(String requestId, Map<String, Integer> results) throws IOException {
        //master.handleReducedResult(requestId, results);
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("REDUCE_MAP_RESULTS", requestId, results));
        out.flush();
    }

    public synchronized void addPartialResult(String requestId, List<Store> mappedResults) throws IOException {
        synchronized (pendingRed){
            pendingRed.computeIfAbsent(requestId, k -> new ArrayList<>()).add(mappedResults);
        }

        if (pendingRed.get(requestId).size() == master.getWorkers().size()) {
            List<Store> finalResult = reduceAllFilterResults(pendingRed.get(requestId));
            synchronized (pendingRed){
                pendingRed.remove(requestId);
            }
            sendToMaster(requestId, finalResult);
        }
    }

    private List<Store> reduceAllFilterResults(List<List<Store>> partialResults) {
        return partialResults.stream()
                .flatMap(List::stream)
                .distinct() // Assuming Store has proper equals() implementation
                .collect(Collectors.toList());
    }

    private void sendToMaster(String requestId, List<Store> results) throws IOException {
        /*
        stelnei ston master ta reduced apotelesmata
         */
        //master.handleReducedResult(requestId, results);
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(new WorkerFunctions("REDUCE_FILTER_RESULTS", requestId, results));
        out.flush();
    }

    private static class ReducerHandler extends Thread {
        /*
        boithitikh klash gia to reduce
         */
        private final Socket socket;
        private final Master master;
        private final Reducer reducer;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ReducerHandler(Socket socket, Master master, Reducer reducer) {
            this.socket = socket;
            this.master = master;
            this.reducer = reducer;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String operation = (String) in.readObject();
                String requestId = (String) in.readObject();

                if(operation.equals("FILTER_STORES")) {
                    List<Store> mappedResults = (List<Store>) in.readObject();
                    reducer.addPartialResult(requestId, mappedResults);
                    //out.writeObject(mappedResults);
                }else{
                    Map<String, Integer> mappedResults = (Map<String, Integer>) in.readObject();
                    reducer.addPartialResult(requestId, mappedResults);
                    //out.writeObject(mappedResults);
                }


                //out.writeObject(Collections.emptyMap());
                //out.flush();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

