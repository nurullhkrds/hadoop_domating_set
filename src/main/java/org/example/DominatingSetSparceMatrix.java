package org.example;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class DominatingSetSparceMatrix {
    private int[][] adjacencyMatrix;
    private ArrayList<Integer> vertexDegrees;
    private ArrayList<Integer> degreeDA;
    private ArrayList<Integer> nodeColors;
    private ArrayList<Integer> VD; // Seçilen düğümler listesi

    public DominatingSetSparceMatrix(String filePath) throws FileNotFoundException {
        vertexDegrees = new ArrayList<>(Collections.nCopies(adjacencyMatrix.length, 0));
        degreeDA = new ArrayList<>(Collections.nCopies(adjacencyMatrix.length, 0));
        nodeColors = new ArrayList<>(Collections.nCopies(adjacencyMatrix.length, 0));
        VD = new ArrayList<>();
    }

    public static int[] degrees(Map<Integer, Set<Integer>> A, int[] dugumRengi, int totalNodes) {
        int[] D = new int[totalNodes]; // Diziyi sıfırlarla başlat

        // A'daki her bir giriş için
        for (int i = 0; i < totalNodes; i++) {
            if (dugumRengi[i] != 2)
            {
                Set<Integer> neighbors = A.get(i);
                if (neighbors != null) {
                    D[i] = neighbors.size();
                } else {
                    D[i] = 5; // bağımsız düğümler
                }
            }

        }

        return D;
    }

    public static int[] degreeDA(Map<Integer, Set<Integer>> A, int[] dugumRengi, int totalNodes) {
        int[] DA = new int[totalNodes]; // Derece Ayarlamalarını saklamak için dizi

        for (Map.Entry<Integer, Set<Integer>> entry : A.entrySet()) {
            int node = entry.getKey();
            if (dugumRengi[node] != 2)
            {
                Set<Integer> neighbors = entry.getValue();

                for (int neighbor : neighbors) {
                    if (dugumRengi[neighbor] == 0) {
                        DA[node] += 1;
                    }
                }
            }
        }
        return DA;
    }

    public static ArrayList<Integer> dugumEkle(int[] D, ArrayList<Integer> VD, int[] dugumRengi, int[] kapsamKumesi,
                                               Map<Integer, Set<Integer>> B) {
        int n = D.length;
        ArrayList<Integer> newVD = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (D[i] == 0 && dugumRengi[i] == 0) {
                VD.add(i);
                newVD.add(i);
                dugumRengi[i] = 2;
            }
        }

        for (Integer w : newVD) {
            Set<Integer> neighbors = B.get(w);
            if (neighbors != null) {
                for (int neighbor : neighbors) {
                    kapsamKumesi[neighbor] += 1;
                }
            }
        }

        return VD;
    }

    public static double[] birinciMalatyaMerkezilik(Map<Integer, Set<Integer>> A, int[] D, double avg, int[] dugumRengi) {
        int n = D.length;
        double[] MC1 = new double[n];
        double alpha1 = 1;
        double beta1 = 1;
        double gamma1 = 1;
        for (int i = 0; i < n; i++) {
            if (dugumRengi[i] != 2)
            {
                Set<Integer> neighbors = A.get(i);
                if (neighbors != null) {
                    for (int neighbor : neighbors) {
                        if (D[neighbor] != 0) {
                            MC1[i] += (double) Math.pow(D[i], beta1)  / Math.pow(D[neighbor], gamma1);
                        }
                    }
                    if (D[i] != 0) {
                        MC1[i] /= (double) Math.pow(D[i], alpha1);
                    }
                }
            }

        }
        return MC1;
    }

    public static double[] ikinciMalatyaMerkezilik(Map<Integer, Set<Integer>> A, int[] D, int[] DA, double[] MC1, double avg, int[] dugumRengi) {
        int n = D.length;
        double[] MC2 = new double[n];

        // double alpha2= 1 + Math.log1p(avg);
        // double beta2 = Math.log1p(avg);
        // double gamma2 = Math.log1p(avg);

        double alpha2= 2;
        double beta2 = 1;
        double gamma2 = 1;

        for (int i = 0; i < n; i++) {
            if ((dugumRengi[i] != 2) && (D[i] > 0)) {
                Set<Integer> neighbors = A.get(i);

                if (neighbors != null) {
                    for (int neighbor : neighbors) {

                        // beta2 = Math.log1p(D[i]); 
                        // gamma2 = Math.log1p(D[i]); 
                        if (D[neighbor] != 0 && MC1[neighbor] != 0) {
                            MC2[i] += Math.pow(MC1[i], beta2) / Math.pow(MC1[neighbor], gamma2);
                        }
                    }
                }
            }
        }

        // MC2 değerlerini DA ile çarp ve D ile ölçekle
        for (int i = 0; i < n; i++) {
            // if(avg <neighbors.size())
            //     alpha = neighbors.size()/alpha;
            // else
            //     alpha = 2;

            if (D[i] != 0) {
                // int neighborCount = A.get(i) != null ? A.get(i).size() : 0;
                // alpha2 = 1 - (avg - neighborCount) / Math.max(1, avg);

                MC2[i] *= DA[i];
                MC2[i] /= Math.pow(D[i], alpha2);
            }
            else{
                MC2[i] = 0;
            }
        }
        return MC2;
    }

    public static Map<Integer, Set<Integer>> dugumSecme(Map<Integer, Set<Integer>> A, int[] D, int[] DA, int[] dugumRengi, double[] MC2,
                                                        ArrayList<Integer> VD, int[] kapsamKumesi, Map<Integer, Set<Integer>> B) {
        int n = A.size();
        int secilenDugum = maxMalatya2Dugum(A, MC2, dugumRengi);

        if (secilenDugum >= 0) {
            VD.add(secilenDugum);
            Set<Integer> neighbors = A.get(secilenDugum);

            if (neighbors != null) {
                for (int neighbor : neighbors) {
                    if (dugumRengi[neighbor] == 0) {
                        dugumRengi[neighbor] = 1;
                        DA[neighbor] -= 1;
                    }
                }
            }

            Set<Integer> BNeighbors = B.get(secilenDugum);
            if (BNeighbors != null) {
                for (int neighbor : BNeighbors) {
                    kapsamKumesi[neighbor] += 1;
                }
            }

            dugumSilme(A, D, DA, dugumRengi, secilenDugum);
            dugumArama(A, D, DA, dugumRengi);
        }
        else {
            for (int i = 0; i < n; i++) {
                if (dugumRengi[i] == 0) {
                    VD.add(i);
                    dugumRengi[i] = 2;

                    Set<Integer> BNeighbors = B.get(i);
                    if (BNeighbors != null) {
                        for (int neighbor : BNeighbors) {
                            kapsamKumesi[neighbor] += 1;
                        }
                    }
                }
            }
        }

        return A;
    }

    public static void dugumArama(Map<Integer, Set<Integer>> A, int[] D, int[] DA, int[] dugumRengi) {
        int n = D.length;

        for (int i = 0; i < n; i++) {
            if (dugumRengi[i] == 1) {
                DA[i] = 0; // İlgili düğümün DA değerini sıfırla

                Set<Integer> neighbors = A.get(i);
                if (neighbors != null) {
                    for (int neighbor : neighbors) {
                        if (dugumRengi[neighbor] == 0) {
                            DA[i] += 1;
                        }
                    }
                }

                if (DA[i] < 2) {
                    D[i] = 0;
                    DA[i] = 0;
                    dugumRengi[i] = 3;

                    if (neighbors != null) {
                        List<Integer> neighborList = new ArrayList<>(neighbors);

                        // Komşuluk ilişkilerini kaldırın
                        for (int neighbor : neighborList) {
                            setToZero(A, i, neighbor);
                        }
                    }
                }
            }
        }
    }

    public static void dugumSilme(Map<Integer, Set<Integer>>  A, int[] D, int[] DA, int[] dugumRengi, int secilenDugum) {
        D[secilenDugum] = 0; // Seçilen düğümün derecesini sıfırla
        DA[secilenDugum] = 0; // Seçilen düğümün DA değerini sıfırla
        dugumRengi[secilenDugum] = 2; // Seçilen düğümün rengini 2 yap

        Set<Integer> neighbors = A.get(secilenDugum);
        if (neighbors != null) {
            // Komşuları geçici bir listede saklayın
            List<Integer> neighborList = new ArrayList<>(neighbors);

            // Komşuluk ilişkilerini kaldırın
            for (int neighbor : neighborList) {
                setToZero(A, secilenDugum, neighbor);
            }
        }
    }


    public static void setToZero(Map<Integer, Set<Integer>> A, int node1, int node2) {
        if (A.containsKey(node1)) {
            Set<Integer> neighbors1 = A.get(node1);
            if (neighbors1 != null) {
                neighbors1.remove(node2);
            }
        }
        if (A.containsKey(node2)) {
            Set<Integer> neighbors2 = A.get(node2);
            if (neighbors2 != null) {
                neighbors2.remove(node1);
            }
        }
    }

    public static void addEdge(Map<Integer, Set<Integer>> sparseMatrix, int node1, int node2) {
        sparseMatrix.computeIfAbsent(node1, k -> new HashSet<>()).add(node2);
        sparseMatrix.computeIfAbsent(node2, k -> new HashSet<>()).add(node1); // Eğer grafiğin yönsüz olduğunu varsayarsak
    }

    public static int bitim(int[] dugumRengi) {
        int n = dugumRengi.length; // DugumRengi dizisinin uzunluğu
        int bitimDurumu = 0; // Başlangıçta BitimDurumu'nu 0 olarak ayarla

        for (int i = 0; i < n; i++) {
            if (dugumRengi[i] == 0) {
                bitimDurumu = 1; // Eğer herhangi bir düğüm işlenmemişse BitimDurumu'nu 1 yap
                break; // Bir düğüm işlenmemişse, döngüyü kır ve bitir
            }
        }

        return bitimDurumu;
    }

    public static int maxMalatya2Dugum(Map<Integer, Set<Integer>> A, double[] MC2, int[] dugumRengi) {
        int n = MC2.length;
        int griMax = 0; // İlk elemanı başlangıç değeri olarak kullan
        int beyazMax = 0; // İlk elemanı başlangıç değeri olarak kullan
        int griKomsu = 0;
        int beyazKomsu = 0;

        // En yüksek merkezilik skorlarına sahip gri ve beyaz düğümleri bul
        for (int i = 1; i < n; i++) {
            if (dugumRengi[i] == 1 && MC2[i] > MC2[griMax]) {
                griMax = i;
            }
            if (dugumRengi[i] == 0 && MC2[i] > MC2[beyazMax]) {
                beyazMax = i;
            }
        }

        // Gri ve beyaz maksimum düğümlerin işlenmemiş komşu sayısını hesapla
        Set<Integer> griNeighbors = A.get(griMax);
        if (griNeighbors != null) {
            for (int neighbor : griNeighbors) {
                if (dugumRengi[neighbor] == 0) {
                    griKomsu++;
                }
            }
        }

        Set<Integer> beyazNeighbors = A.get(beyazMax);
        if (beyazNeighbors != null) {
            for (int neighbor : beyazNeighbors) {
                if (dugumRengi[neighbor] == 0) {
                    beyazKomsu++;
                }
            }
        }

        if (MC2[griMax] == 0 && MC2[beyazMax] == 0 && griKomsu == 0 && beyazKomsu == 0)
            return -1;

        // Karar verme aşaması: Hangi düğüm seçilecek?
        if (MC2[griMax] > MC2[beyazMax] && griKomsu > beyazKomsu) {
            return griMax;
        } else {
            return beyazMax;
        }
    }

    public static Map<Integer, Set<Integer>> getAdjacencyMatrix(String path, ArrayList<Integer> nodes, List<int[]> edges) throws IOException {
        int nodeNum = 0;
        Set<Integer> nodeSet = new HashSet<>();
        Map<Integer, Integer> nodeIndexMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Satırı temizleyip virgüllerle ayır
                String[] parts = line.trim().split(",");
                int node1 = Integer.parseInt(parts[0].trim());
                int node2 = Integer.parseInt(parts[1].trim());
                if (node1 > nodeNum)
                    nodeNum = node1;
                if (node2 > nodeNum)
                    nodeNum = node2;

                if (node1 != node2)
                    edges.add(new int[]{node1, node2});

                nodeSet.add(node1);
                nodeSet.add(node2);
            }
        }

        nodes.addAll(nodeSet);

        // Düğümlerin sayısı
        int index = 0;
        for (Integer node : nodes) {
            nodeIndexMap.put(node, index++);
        }

        Map<Integer, Set<Integer>> adjMatrix = new HashMap<>();

        // Kenarları bitişiklik matrisine dönüştür
        for (int[] edge : edges) {
            int col1 = nodeIndexMap.get(edge[0]);
            int col2 = nodeIndexMap.get(edge[1]);

            adjMatrix.computeIfAbsent(col1, k -> new HashSet<>()).add(col2);
            adjMatrix.computeIfAbsent(col2, k -> new HashSet<>()).add(col1);
        }

        return adjMatrix;
    }

    private static int[][] deepCopy(int[][] original) {
        if (original == null)
            return null;
        int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

    public static Map<Integer, Set<Integer>> deepCopySparseMatrix(Map<Integer, Set<Integer>> original) {
        Map<Integer, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    private static void fineTuning(Map<Integer, Set<Integer>> A, int[] kapsamKumesi, ArrayList<Integer> VD) {

        ArrayList<Integer> removedList = new ArrayList<>();
        ArrayList<Integer> addedList = new ArrayList<>();
        Set<Integer> vdSet = new HashSet<>(VD);

        for (int secilen : VD) {
            boolean notTuned = false;
            ArrayList<Integer> komsuList = new ArrayList<>();
            Set<Integer> neighbors = A.get(secilen);

            if (neighbors != null) {
                for (int neighbor : neighbors) {
                    if (!vdSet.contains(neighbor)) {
                        if (kapsamKumesi[neighbor] < 2) {
                            notTuned = true;
                        } else {
                            komsuList.add(neighbor);
                        }
                    }
                }
            }

            if (!notTuned) {
                for (Integer komsu : komsuList) {
                    boolean istuned = false;
                    Set<Integer> komsuNeighbors = A.get(komsu);

                    if (komsuNeighbors != null) {
                        for (int neighbor : komsuNeighbors) {
                            if (vdSet.contains(neighbor) && neighbor != secilen) {
                                boolean canTuned = true;
                                Set<Integer> neighborNeighbors = A.get(neighbor);

                                if (neighborNeighbors != null) {
                                    for (int t : neighborNeighbors) {
                                        if (!vdSet.contains(t) && kapsamKumesi[t] < 2
                                                && (A.get(secilen) == null || !A.get(secilen).contains(t))) {
                                            canTuned = false;
                                            break;
                                        }
                                    }
                                }

                                if (canTuned && !removedList.contains(neighbor)
                                        && !removedList.contains(secilen) && !addedList.contains(komsu)) {
                                    addedList.add(komsu);
                                    removedList.add(neighbor);
                                    removedList.add(secilen);
                                    istuned = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (istuned) break;
                }
            }
        }

        System.out.println("Removed Dominating Set Number: " + removedList.size());
        System.out.println("Added Dominating Set Number: " + addedList.size());

        for (Integer integer : removedList) {
            VD.remove(integer);
        }
        VD.addAll(addedList);

    }

    private static boolean contains(ArrayList<Integer> VD, int i) {
        for (Integer integer : VD) {
            if (integer == i)
                return true;
        }
        return false;
    }

    public static void main(String[] args) {
        try {

            List<int[]> edges = new ArrayList<>();
            ArrayList<Integer> nodes = new ArrayList<>();
            String dataname="grid5x5";
            System.out.println("Dataset: " + dataname);
            Map<Integer, Set<Integer>> A = getAdjacencyMatrix(dataname + ".txt", nodes, edges);
            System.out.println("Nodes size: " + nodes.size());
            System.out.println("Edges size: " + edges.size());

            Map<Integer, Set<Integer>> B = deepCopySparseMatrix(A);
            Date dt1 = new Date();
            System.out.println("Tick: " + dt1.toString());
            int n = nodes.size();
            ArrayList<Integer> VD = new ArrayList<>();
            int[] D = new int[n];
            int[] DA = new int[n];
            double[] MC1 = new double[n];
            double[] MC2 = new double[n];
            int[] dugumRengi = new int[n];
            int[] kapsamKumesi = new int[n];
            double avgRelation = (double) edges.size()/nodes.size();
            // Initialize degrees and DA
            D = degrees(A, dugumRengi,  nodes.size());
            DA = degreeDA(A, dugumRengi, nodes.size());

            // for graphical output
            boolean isPlotting = false; // Set to true if visualization is enabled

            int bitimDurumu = 1;

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int zeroCount = 0;
                    for (int color : dugumRengi) {
                        if (color == 0) {
                            zeroCount++;
                        }
                    }
                    double percentage = (double) zeroCount / dugumRengi.length * 100;
                    System.out.printf("Kalan düğüm sayısı: %.2f%%%n", percentage);
                }
            }, 0, 600000);

            while (bitimDurumu == 1) {
                MC1 = birinciMalatyaMerkezilik(A, D,avgRelation, dugumRengi);
                MC2 = ikinciMalatyaMerkezilik(A, D, DA, MC1,avgRelation, dugumRengi);
                A = dugumSecme(A, D, DA, dugumRengi, MC2, VD, kapsamKumesi, B); // Adjust return type as needed
                D = degrees(A,dugumRengi, nodes.size());
                DA = degreeDA(A, dugumRengi, nodes.size());
                VD = dugumEkle(D, VD, dugumRengi, kapsamKumesi, B); // Adjust return type as needed
                bitimDurumu = bitim(dugumRengi);

                // Visualize the graph if plotting is enabled
                if (isPlotting) {
                    // codes comes here
                }
            }

            fineTuning(B, kapsamKumesi, VD);
            Date dt2 = new Date();

            //System.out.println("Selected Nodes: " + VD);

            System.out.print("\033[42m");
            System.out.println("Dominating Set Number: " + VD.size() + "\033[0m");
            System.out.print("\033[0m");
            System.out.println("");
            System.out.println("Tock: " + dt2.toString());
            long seconds = (dt2.getTime()-dt1.getTime())/1000;
            System.out.println("Run Time in Seconds: " + seconds);


            long miliseconds = (dt2.getTime()-dt1.getTime());
            System.out.println("Run Time in Miliseconds: " + miliseconds);

            timer.cancel();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}