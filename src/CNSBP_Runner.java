import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;


public class CNSBP_Runner {

    public static void main(String[] args) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("/Users/leongtinjet/Downloads/BPP.txt"));
        String line;
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

                for(int q=0;q< quantity ;q++){
                    items.add(new Item(weight));
                    id++;
                }
            }
            Problem problem = new Problem(items,capacity);
            CNS_BP binPacking = new CNS_BP(problem); // Set capacity to 10 for example
            System.out.println(problemName);
            System.out.println("total: bin " + binPacking.runMainLoop().bins.size());
            System.out.println("trashcan remaining: " + problem.getTrashCan().getTotalWeight());
            Bin.counter = 1;

        }
        reader.close();



    }
}
