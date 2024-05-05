import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.util.Map;


public class CNSBP_Runner {
    public static boolean converged = false;
    public static HashMap<String, Double> totalTime = new HashMap<>();
    public static HashMap<String, Integer> optimalBin = new HashMap<>();
    public static HashMap<String, Integer> wastedSpace = new HashMap<>();



    public static void main(String[] args) throws IOException {
        final int maxIter = 1;
        for (int iter = 0; iter < maxIter; iter++) {

            BufferedReader reader = new BufferedReader(new FileReader("src/BPP.txt"));

            String line;

            System.out.println("Iteration "+ iter);

            // Read item weights and quantities
            while ((line = reader.readLine()) != null) {
                int id = 0;
                List<Item> items = new ArrayList<>();
                int capacity = 0;
                // Read the problem name
                String problemName = line.trim();
                // Read the line "number m of different item weights"
                int numWeights = Integer.parseInt(reader.readLine().trim());
                // Read the capacity of the bins
                capacity = Integer.parseInt(reader.readLine().trim());

                for (int i = 0; i < numWeights; i++) {
                    String[] itemLine = reader.readLine().trim().split("\\s+");
                    int weight = Integer.parseInt(itemLine[0]); // Parse weight
                    int quantity = Integer.parseInt(itemLine[1]); // Parse quantity

                    for (int q = 0; q < quantity; q++) {
                        items.add(new Item(weight));
                        id++;
                    }
                }

                // if (!problemName.trim().equalsIgnoreCase("'TEST0049'")) {
                Problem problem = new Problem(items, capacity,problemName);
                CNS_BP binPacking = new CNS_BP(problem);

                System.out.printf("\n" +problemName+ "\n");
                int prob = 0;
                for (Item i : problem.getAllItems()) {
                    prob += i.getWeight();
                }

                System.out.println("Total weight of problem= " + prob);
                Solution finalSolution = binPacking.runMainLoop();
                System.out.println("Final total bin number: " + finalSolution.bins.size());
                System.out.println("Trashcan total remaining item weight: " + problem.getTrashCan().getTotalWeight());

                if (optimalBin.containsKey(problemName)) {
                    // Key exists, so add the number to the existing value
                    int currentValue = optimalBin.get(problemName);
                    int newValue = currentValue + finalSolution.getNumBins();
                    optimalBin.put(problemName, newValue);
                } else {
                    // Key doesn't exist, so add it with the provided value
                    optimalBin.put(problemName, finalSolution.getNumBins());
                }

                if (wastedSpace.containsKey(problemName)) {
                    // Key exists, so add the number to the existing value
                    int currentValue = wastedSpace.get(problemName);
                    int newValue = currentValue + finalSolution.getTotalWastedCapacity();
                    wastedSpace.put(problemName, newValue);
                } else {
                    // Key doesn't exist, so add it with the provided value
                    wastedSpace.put(problemName, finalSolution.getTotalWastedCapacity());
                }

                // Reset counter
                Bin.counter = 1;

            }
            reader.close();
        }

        System.out.println();

        for (Map.Entry<String, Double> entry : totalTime.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            System.out.println("Problem: " + key + ", Average time: " + (value / maxIter)+ " second"); // Casting value to double
        }
        System.out.println();
        for (Map.Entry<String, Integer> entry : optimalBin.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("Problem: " + key + ", Average bin: " + ((double)value / maxIter) + " bins"); // Casting value to double
        }
        System.out.println();
        for (Map.Entry<String, Integer> entry : wastedSpace.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("Problem: " + key + ", Average wasted space: " + ((double)value / maxIter)); // Casting value to double
        }
    }
}
