import java.util.*;

//https://afros.tdasociety.org/wp-content/uploads/2018/06/AFROS_2018_paper_71.pdf
public class CNS_BP {
    static Problem problem;

    public CNS_BP(Problem problem) {
        this.problem = problem;
    }

    public Solution runMainLoop() {
        //remove item pairs (i,j) such that w_i + w_j = C
        List<Bin> binsWithPairs = createBinsWithPairs();

        //compute lower bound LB
        List<Item> items = problem.getAllItems();
        int LB = computeLowerBound(items);
        System.out.println("Lowerbound: " + LB);

        //randomly shuffle the set of items
        Collections.shuffle(items);

        //S ← complete solution obtained by First Fit
        Solution S = firstFit(items);
        S.printNumBin();

        //m ← number of bins in S
        int m = S.bins.size();
        long startTime = System.currentTimeMillis();
        long timeLimit = 6000;

        //While (m > LB  and time limit not exceeded)
        while (m > LB && (System.currentTimeMillis() - startTime) < timeLimit) {

            //build partial solution P with m − 2 bins ▹ // delete 3 bins from S
            Solution partialSolution = buildPartialSolution(S);
            System.out.println("Bin: "+ partialSolution.getNumBins());

            //try to find complete solution with m bins
            Solution SPrime = CNS(partialSolution);

            //If solution S′ not complete, then break
            if (!SPrime.isSolutionComplete()) {
                System.out.println("break");
                System.out.println(problem.getTrashCan().getTotalItem());
                problem.getTrashCan().print();
                break;
            }

            //S ← S ′
            S = SPrime;
            //update the current bin number
            m = S.bins.size();
        }

        //Add those bin with item that we removed in the first step to the solution
        for (Bin bin : binsWithPairs) {
            System.out.println("bin added");
            S.bins.add(bin);
        }

        //return S ▹ return the last complete solution
        return S;
    }

    private Solution buildPartialSolution(Solution S) {
        List<Bin> tempBins = new ArrayList<>(S.bins);
        int numBins = tempBins.size();

        int removedBin = 0;
        while (removedBin != 3) {
            boolean limitExceed = false;

                int lastBinIndex = numBins - removedBin -1; // Last bin index excluding the last two
                Bin lastBin = tempBins.get(lastBinIndex);
                int bigItemsCount = 0;

                // Count the number of "big" items in the last bin
                for (Item item : lastBin.getItemsList()) {
                    if (item.getWeight() > problem.getCapacity() / 2) {
                        bigItemsCount++;
                    }
                }

                // Check if bigItemsCount does not exceed 2
                if (bigItemsCount <= 2) {
                    // Move items from the last bin to the trash can
                    for (Item item : lastBin.getItemsList()) {
                    problem.getTrashCan().addItem(item);
                    }
                    tempBins.remove(lastBinIndex); // Remove the last bin
                } else {
                    limitExceed = true;
                }

                if (!limitExceed) {
                    removedBin++;
                }
        }

        Solution partialSolution = new Solution(problem, tempBins);
        return partialSolution;
    }

    private Solution CNS(Solution partialSolution) {
        Tabu tabu = new Tabu(partialSolution,100,1,problem);
        long startTime = System.currentTimeMillis();
        long timeLimit = 60000;

        // TODO add time or iterations limit not exceeded to the while condition
         while (!partialSolution.isSolutionComplete() &&  (System.currentTimeMillis() - startTime) < timeLimit) {

        Solution temp = partialSolution;
        partialSolution = tabu.tabuSearch(partialSolution);
//        System.out.println(temp.getObjectiveFunction());
//        System.out.println(partialSolution.getObjectiveFunction());

        partialSolution = Descent(partialSolution);
//        System.out.println(temp.getObjectiveFunction());
//        System.out.println(partialSolution.getObjectiveFunction());
             System.out.print("[ " );
             for(Bin bin: partialSolution.bins){
                 System.out.print(bin.getRemainingCapacity()+ " , ");
             }
             System.out.println(  " ] " + partialSolution.getTotalWastedCapacity());

          }

        System.out.println("Solution complete: " + partialSolution.isSolutionComplete());

        return partialSolution;
    }

    public static List<Item> PStar = new ArrayList<>();

    // input: set of items S, current packing P (initialized to empty set)
    public static List<Item> PackSet(List<Item> S, List<Item> P) {
        // If S is empty
        if (S.isEmpty()) {
            // If w(P) > w(P*) or w(P) = w(P*) and |P| < |P*|
            if (weight(P) > weight(PStar) || (weight(P) == weight(PStar) && P.size() < PStar.size())) {
                // P* ← P
                PStar = P;
            }
        } else {
            // i ← first item in S
            Item i = S.get(0);
            // S' ← S \ {i} // Remove i from the set of items
            List<Item> newS = new ArrayList<>(S);
            newS.remove(i);
            // If (w(P) + w_i ≤ C) // if the current packing weight + weight of i is less than capacity
            if (weight(P) + i.getWeight() <= problem.getCapacity()) {
                // pack_set(S', P ∪ {i}) // call the function recursively with i added to P
                List<Item> newP = new ArrayList<>(P);
                newP.add(i);
                PackSet(newS, newP);
            }
            // pack_set(S', P)
            PackSet(newS, P);
        }
        // output: best packing P* (static variable, initialized to empty set)
        return PStar;
    }

    /*optimally rearrange the items between bin b ∈ B and
 trash can TC, such that the remaining capacity in b is minimized,
 that is, the set of items assigned to bin b fits the bin capacity as
 tightly as possible. Pack is a generalization of a Swap move with
 p and q both being unlimited.*/
    public Solution packB(Solution solution, int binIndex) {
        Solution newSolution = solution.copy();
        Bin updatedBin = newSolution.getBins().get(binIndex);

        // Sort the items in the trash can in descending order of weight
        List<Item> trashCanItems = problem.getTrashCan().getItemsList();
        Collections.sort(trashCanItems, Collections.reverseOrder());

        List<Item> itemsToRemove = new ArrayList<>(); // List to hold items to remove

        // Iterate through the items in the trash can
        for (Item item : trashCanItems) {
            if (updatedBin.addItem(item)) {
                // If the item was added to the bin, mark it for removal
                itemsToRemove.add(item);
//                System.out.println(item.isInBin());
            }
        }

        // Remove the marked items from the trash can
        for (Item item : itemsToRemove) {
            problem.getTrashCan().removeItem(item);
        }

        return newSolution;
    }


    public Solution Descent(Solution partialSol) {
        Solution newSolution = partialSol.copy();
        int capacity = problem.getCapacity();
        List<Bin> newBins;
        boolean packed = false;
        do {

            List<Bin> bins = partialSol.getBins();
            //Collections.shuffle(bins);
            for (Bin bin : bins) {
                newSolution = packB(partialSol, partialSol.bins.indexOf(bin));

                if (problem.getTrashCan().getTotalWeight() <= 2 * capacity
                 || weight(PackSet(problem.getTrashCan().getItemsList(), new ArrayList<>())) >= weight(problem.getTrashCan().getItemsList()) - capacity
                ) {
                    newBins = packTrashcanIntoNewBins();
                    newSolution.getBins().addAll(newBins);
                    packed = true;
                    break;
                }
            }

//            System.out.println(newSolution.getObjectiveFunction());
//            System.out.println(partialSol.getObjectiveFunction());
            if (newSolution.getObjectiveFunction() <= partialSol.getObjectiveFunction() || packed) {
                break;
            }

        } while (true);

        return newSolution;
    }

    //Tested: pack all item in trash can into 2 bin when w(TC)≤ 2C
    public List<Bin> packTrashcanIntoNewBins() {

        Bin bin1 ;
        Bin bin2 ;
        List<Item> tc = new ArrayList<>(problem.getTrashCan().getItemsList()); // Create a new list to avoid ConcurrentModificationException

        Collections.sort(tc, Collections.reverseOrder());
        List<Item> itemsToRemove; // List to hold items to remove

        while (true) {
            boolean fullPacked = true;
            bin1 = new Bin(problem.getCapacity());
            bin2 = new Bin(problem.getCapacity());
            itemsToRemove = new ArrayList<>();
            System.out.println("Trashcan initial : " + problem.getTrashCan().getTotalWeight());
            for (Item item : tc) {
                if (bin1.addItem(item)) {
                    // If the item was added to bin1, mark it for removal
                    itemsToRemove.add(item);
                } else if (bin2.addItem(item)) {
                    // If the item was added to bin2, mark it for removal
                    itemsToRemove.add(item);
                } else {
                    fullPacked = false;
                }
            }

            if (fullPacked) {
                break;
            }
            Collections.shuffle(tc);
        }

        // Remove the marked items from the trash can
        for (Item item : itemsToRemove) {
            problem.getTrashCan().removeItem(item);
        }

        List<Bin> newBins = new ArrayList<>();
        newBins.add(bin1);
        newBins.add(bin2);

        return newBins;
    }





    /*******************************************************************************************/

    private int computeLowerBound(List<Item> items) {
        int totalWeight = 0;
        for (Item item : items) {
            totalWeight += item.getWeight();
        }
        return totalWeight / problem.getCapacity();
    }

    private Solution firstFit(List<Item> items) {
        List<Bin> bins = new ArrayList<>();
        for (Item item : items) {
            boolean placed = false;
            for (Bin bin : bins) {
                if (bin.getRemainingCapacity() >= item.getWeight()) {
                    bin.addItem(item);
                    placed = true;
                    item.setInBin(true);
                    break;
                }
            }
            if (!placed) {
                Bin newBin = new Bin(problem.getCapacity());
                newBin.addItem(item);
                bins.add(newBin);
            }
        }
        Solution solution = new Solution(problem, bins);

        return solution;
    }

    private List<Bin> createBinsWithPairs() {
        List<Bin> bins = new ArrayList<>();

        // Iterate through all items to find pairs
        for (int i = 0; i < problem.getAllItems().size(); i++) {
            Item item1 = problem.getAllItems().get(i);

            // Skip items that have already been placed in a bin
            if (item1.isInTrashCan()) {
                continue;
            }

            for (int j = i + 1; j < problem.getAllItems().size(); j++) {
                Item item2 = problem.getAllItems().get(j);

                // Skip items that have already been placed in a bin
                if (item2.isInBin()) {
                    continue;
                }

                // Check if the sum of weights equals bin capacity
                if (item1.getWeight() + item2.getWeight() == problem.getCapacity()) {
                    // Create a new bin and add the items
                    Bin bin = new Bin(problem.getCapacity());
                    bin.addItem(item1);
                    bin.addItem(item2);

                    // Mark items as placed in a bin
                    item1.setInBin(true);
                    item2.setInBin(true);

                    // Add the bin to the list
                    bins.add(bin);

                    // Exit the inner loop since we found a pair
                    break;
                }
            }
        }

        // Remove items that were placed in a bin from the items list
        problem.getAllItems().removeIf(Item::isInBin);

        return bins;
    }

    public static int weight(List<Item> items) {
        int weight = 0;
        for (Item item : items) {
            weight += item.getWeight();
        }
        return weight;
    }

}

