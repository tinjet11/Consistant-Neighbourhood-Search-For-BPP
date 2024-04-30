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
        long startTime = System.currentTimeMillis();
        //S ← complete solution obtained by First Fit
        //Solution S = firstFitDecreasing(items);
        Solution S = firstFit(items);
        S.printNumBin();

        //m ← number of bins in S
        int m = S.bins.size();

        long timeLimit = 60000;

        //While (m > LB  and time limit not exceeded)
        while (m > LB && (System.currentTimeMillis() - startTime) < timeLimit) {
            //m = m- 1;
            //build partial solution P with m − 2 bins ▹ // delete 3 bins from S
            Solution temp = S.copy();
            Solution partialSolution = buildPartialSolution(temp);
            System.out.println("Bin: " + partialSolution.getNumBins());

            //try to find complete solution with m bins
            Solution SPrime = CNS(partialSolution);

            for(Bin b: SPrime.bins){
                System.out.print(b.getRemainingCapacity() + ", ");
            }

            //If solution S′ not complete, then break
            if (!SPrime.isSolutionComplete()) {
                System.out.println("Total wasted space: " + SPrime.getTotalWastedCapacity());
                System.out.println("Solution not complete so break");
                System.out.println(problem.getTrashCan().getTotalItem());
                problem.getTrashCan().print();
                Solution nonAssigned = firstFitDecreasing(problem.getTrashCan().getItemsList());
                SPrime.getBins().addAll(nonAssigned.getBins());
                S = SPrime;
                //problem.getTrashCan().getItemsList().clear();
                break;
            }

            //S ← S ′
            S = SPrime;

            //update the current bin number
            m = S.bins.size() ;
        }

        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
        String problemName  = problem.getProblemName() ;
        if (CNSBP_Runner.totalTime.containsKey(problemName)) {
            // Key exists, so add the number to the existing value
            double currentValue = CNSBP_Runner.totalTime.get(problemName);
            double newValue = currentValue + elapsedTime;
            CNSBP_Runner.totalTime.put(problemName, newValue);
        } else {
            // Key doesn't exist, so add it with the provided value
            CNSBP_Runner.totalTime.put(problemName, elapsedTime);
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

            int lastBinIndex = numBins - removedBin - 1; // Last bin index excluding the last two
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

        long startTime = System.currentTimeMillis();
        long timeLimit = 1000;
        Tabu tabu = new Tabu(partialSolution, 1, problem);
        // TODO add time or iterations limit not exceeded to the while condition
        //while (!partialSolution.isSolutionComplete() && (System.currentTimeMillis() - startTime) < timeLimit) {
        while (!partialSolution.isSolutionComplete() && !CNSBP_Runner.converged) {

            partialSolution = tabu.tabuSearch(partialSolution);
            partialSolution = Descent(partialSolution);

        }
        //set back to false
        CNSBP_Runner.converged = false;
        return partialSolution;
    }

    public static List<Item> PStar = new ArrayList<>();

    public static List<Item> PackSet(List<Item> S, List<Item> P) {
        return PackSetHelper(S, P, new HashMap<>());
    }

    private static List<Item> PackSetHelper(List<Item> S, List<Item> P, Map<List<Item>, List<Item>> cache) {
        // Sort S in descending order of item weights
        Collections.sort(S, (a, b) -> b.getWeight() - a.getWeight());

        // Check if the result is already in the cache
        List<Item> cacheResult = cache.get(S);
        if (cacheResult != null) {
            return cacheResult;
        }

        if (S.isEmpty()) {
            if (weight(P) > weight(PStar) || (weight(P) == weight(PStar) && P.size() < PStar.size())) {
                PStar = new ArrayList<>(P);
            }
            cache.put(new ArrayList<>(), PStar);
            return PStar;
        }

        Item i = S.get(0);
        List<Item> newS = new ArrayList<>(S);
        newS.remove(0);

        List<Item> result = PStar;

        // Early pruning
        int remainingWeight = weight(newS);
        if (remainingWeight + weight(P) <= problem.getCapacity()) {
            // Attempt to add i to P if it fits
            if (weight(P) + i.getWeight() <= problem.getCapacity()) {
                P.add(i);
                result = PackSetHelper(newS, P, cache);
                P.remove(i);
            }

            // Recur without adding i to P
            result = PackSetHelper(newS, P, cache);
        }

        cache.put(S, result);
        return result;
    }

    /*optimally rearrange the items between bin b ∈ B and
 trash can TC, such that the remaining capacity in b is minimized,
 that is, the set of items assigned to bin b fits the bin capacity as
 tightly as possible. Pack is a generalization of a Swap move with
 p and q both being unlimited.*/
    public Solution packB(Solution solution, int binIndex) {
        Solution newSolution = solution.copy();
        Bin updatedBin = newSolution.getBins().get(binIndex);

        List<Item> trashCanItems = problem.getTrashCan().getItemsList();
        trashCanItems.sort(Collections.reverseOrder());

        for (Iterator<Item> iterator = trashCanItems.iterator(); iterator.hasNext(); ) {
            Item item = iterator.next();
            if (updatedBin.addItem(item)) {
                //System.out.println("Item packed: " + item.getWeight());
                iterator.remove();
                break;
            }
        }



        return newSolution;
    }

    public Solution Descent(Solution partialSol) {
        Solution newSolution = partialSol.copy();
        int capacity = problem.getCapacity();
        List<Bin> newBins;
        boolean packed = false;

        do {
            if (problem.getTrashCan().getTotalWeight() == 0) {
                break;
            }
            for (Bin bin : newSolution.getBins()) {
                Solution packedSolution = packB(newSolution, newSolution.getBins().indexOf(bin));
                newSolution = packedSolution;

                if (problem.getTrashCan().getTotalWeight() == 0) {
                    break;
                }

                  //  System.out.println("error");

              //  System.out.println(" " + problem.getTrashCan().getTotalWeight());
                PStar = new ArrayList<>();
                if (problem.getTrashCan().getTotalWeight() <= 2 * capacity
                        && weight(PackSet(problem.getTrashCan().getItemsList(), new ArrayList<>())) >= problem.getTrashCan().getTotalWeight() - capacity) {
                    //System.out.println("Before: "+ newSolution.getNumBins());
                    newBins = packTrashcanIntoNewBins();
                    packedSolution.bins.add(newBins.get(0));
                    packedSolution.bins.add(newBins.get(1));
                    newSolution = packedSolution;
                  //  System.out.println("After: "+ newSolution.getNumBins());

                    packed = true;
                    break;
                }

            }
//            System.out.println("New: " + newSolution.getObjectiveFunctionValue());
//           System.out.println("Old: " + partialSol.getObjectiveFunctionValue());
            if (packed || newSolution.getObjectiveFunctionValue() <= partialSol.getObjectiveFunctionValue()) {
              //  System.out.println("New:");
                break;
            }

        } while (true);

        return newSolution;
    }

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
//        Solution nonAssigned = CNS_BP.firstFitDecreasing(problem.getTrashCan().getItemsList());
//
//        List<Bin> newBins = new ArrayList<>();
//        newBins.addAll(nonAssigned.getBins());
//        System.out.println(newBins.size());

        return newBins;
    }



    /*******************************************************************************************/

    private int computeLowerBound(List<Item> items) {
        int totalWeight = 0;
        for (Item item : items) {
            totalWeight += item.getWeight();
        }
        int lowerBound = totalWeight / problem.getCapacity();
        if (totalWeight % problem.getCapacity() != 0) {
            lowerBound++; // Round up if there's a remainder
        }
        return lowerBound;
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

    public static Solution firstFitDecreasing(List<Item> items) {
        // Custom comparator for sorting items in descending order of weight
        Comparator<Item> weightComparator = Comparator.comparingInt(Item::getWeight).reversed();
        items.sort(weightComparator);

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

