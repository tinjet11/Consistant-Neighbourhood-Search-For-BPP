//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.Random;
//
//public class Tabu {
//    public String name;
//    public int items_count;
//    public int max_bin_Size;
//    public int known_optimum;
//    public int local_optimum;
//    public ArrayList<Item> ItemsList = new ArrayList<>();
//    Problem problem;
//
//    public Tabu(Problem problem){
//        this.problem = problem;
//    }
//
//    public Solution TabuSearch(Solution partialSolution) {
//       Solution sol = partialSolution.copy();
//       Solution bestSol = partialSolution.copy();
//
//       int maxIter = 100;
//       int iter = 0;
//
//       //resetTabu();
//        long startTime = System.currentTimeMillis();
//        long timeLimit = 600;
//
//        while(iter < maxIter && (System.currentTimeMillis() - startTime) < timeLimit) {
//         SPrime  = {}
//
//        }
//
//    }
//
//    private void swap(Solution sol, Bin bin, int indexFromBin, int indexFromTrash) {
//        Item itemFromBin = bin.getItemById(indexFromBin);
//        Item itemFromTrash = problem.getTrashCan().getItemsList().get(indexFromTrash);
//
//        if (itemFromBin != null && itemFromTrash != null) {
//            bin.removeItem(itemFromBin);
//            bin.addItem(itemFromTrash);
//
//            problem.getTrashCan().removeItem(itemFromTrash);
//            problem.getTrashCan().addItem(itemFromBin);
//
//            // Update the ItemsList
//            ItemsList.clear();
//            for (Bin b : sol.getBins()) {
//                ItemsList.addAll(b.getItemsList());
//            }
//            ItemsList.addAll(problem.getTrashCan().getItemsList());
//        } else {
//            System.err.println("Error: Item is null in swap method.");
//        }
//    }
//
//    // Helper method to compare two lists of items
//    private boolean compareItemsLists(ArrayList<Item> list1, ArrayList<Item> list2) {
//        if (list1.size() != list2.size()) {
//            return false;
//        }
//
//        for (Item item : list1) {
//            if (!list2.contains(item)) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//}
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Tabu {
    private Solution bestSol;
    private Solution sol;
    private int iter;
    private final int maxNmbIters;
    private final double timeLimitTabu;
    private final Problem problem;
    private final int[][] freq;
    private final int[][] tabu;

    public Tabu(Solution initSol, int maxNmbIters, double timeLimitTabu, Problem problem) {
        this.sol = initSol.copy();
        this.bestSol = initSol.copy();
        this.iter = 0;
        this.maxNmbIters = maxNmbIters;
        this.timeLimitTabu = timeLimitTabu;
        this.problem = problem;

        // Initialize freq and tabu arrays
        freq = new int[initSol.getNumBins()+1][problem.getMaxItemWeight() + 1];
        tabu = new int[initSol.getNumBins()+1][problem.getMaxItemWeight() + 1];
        resetTabu();
    }

    public Solution tabuSearch(Solution solution) {
        this.sol = solution.copy();
        this.bestSol = solution.copy();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (long) (timeLimitTabu * 1000);

        List<List<Item>> subsets;
        iter = 0;
        resetTabu();
        while (iter < maxNmbIters && System.currentTimeMillis() < endTime) {
            List<Item> SStar = new ArrayList<>();
            List<Item> TStar = new ArrayList<>();
            Bin bStar = null;
            int minDelta = sol.getNumBins() * CNS_BP.weight(problem.getAllItems());

            for (Bin bin : sol.getBins()) {
                subsets = generateSubsets(bin.getItemsList());

                for (List<Item> S : subsets) {
                    if (!isTabu(bin, S)) {
                        for (List<Item> T : subsets) {
                            if (isAllowedPair(S, T)) {
                                int totalWeightBin = bin.getTotalWeight();
                                int weightS = getSubsetWeight(S);
                                int weightT = getSubsetWeight(T);

                                // S = item to be move to trashcan
                                // T = item to be move to bin
                                if (totalWeightBin + weightT - weightS <= problem.getCapacity()) {

                                    int delta = sol.getNumBins() * (weightS -weightT) - (S.size() - T.size());
                                    if (delta <= minDelta) {
                                       // System.out.println("improve?");
                                        minDelta = delta;
                                        SStar = S;
                                        TStar = T;
                                        bStar = bin;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (TStar.isEmpty()) {
                break; // Terminate when no allowed move exists
            }

            performSwap(SStar, TStar, bStar);
            if (sol.getObjectiveFunction() < bestSol.getObjectiveFunction()) {
                bestSol = sol;
                resetTabu();
            }

            if (problem.getTrashCan().getTotalWeight() <= 2 * problem.getCapacity()
                &&  (CNS_BP.weight(CNS_BP.PackSet(problem.getTrashCan().getItemsList(), new ArrayList<>())) >= CNS_BP.weight(problem.getTrashCan().getItemsList()) - problem.getCapacity()))
                    {
                packTrashcanIntoNewBins();
                break; // Terminate when conditions met
            }

            updateTabu(bStar, SStar);

            iter++;
        }

        if(bestSol != sol)
        {
//            System.out.println("Best col return");
            return bestSol;
        }else{
            return sol;
        }
    }


    private List<List<Item>> generateSubsets(List<Item> items) {
        int maxSize = 3;
        List<List<Item>> subsets = new ArrayList<>();
        int n = items.size();
        Collections.shuffle(items);
        for (int i = 0; i < (1 << n); i++) {
            List<Item> subset = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    subset.add(items.get(j));
                }
            }
            if (subset.size() <= maxSize) {
                subsets.add(subset);
            }
        }

        Collections.shuffle(subsets);
        return subsets;
    }


    private boolean isAllowedPair(List<Item> S, List<Item> T) {
        int weightS = getSubsetWeight(S);
        int weightT = getSubsetWeight(T);
        return weightT != weightS && S.size() < T.size() ; // Adjust as needed for allowed pairs
    }

    private int getSubsetWeight(List<Item> subset) {
        return subset.stream().mapToInt(Item::getWeight).sum();
    }

    private void performSwap(List<Item> SStar, List<Item> TStar, Bin bStar) {

        List<Item> itemsToRemove = new ArrayList<>();
        List<Item> itemsToAddToTrash = new ArrayList<>();

        // S = item to be move to trashcan
        // T = item to be move to bin
        for (Item item : SStar) {
            bStar.removeItem(item);
            itemsToAddToTrash.add(item);
            //System.out.println("Item add to trash can : " + item.getWeight());
        }
        // S = item to be move to trashcan
        // T = item to be move to bin
        for (Item item : TStar) {
            itemsToRemove.add(item);
            bStar.addItem(item);
          //  System.out.println("Item remove from trash can : " + item.getWeight());
        }

        // Remove the marked items from the trash can
        for (Item item : itemsToRemove) {
            problem.getTrashCan().removeItem(item);
        }

        // Add items back to bin if needed
        for (Item item : itemsToAddToTrash) {
            problem.getTrashCan().removeItem(item);
            bStar.addItem(item);
        }
    }

    private boolean isTabu(Bin bin, List<Item> S) {
        for (Item item : S) {
            if (tabu[bin.getId()][item.getWeight()] >= iter) {
                return true;
            }
        }
        return false;
    }

    private void updateTabu(Bin bin, List<Item> S) {
        for (Item item : S) {
            freq[bin.getId()][item.getWeight()]++;
            tabu[bin.getId()][item.getWeight()] = iter + freq[bin.getId()][item.getWeight()] / 2;
        }

        // Reset tabu status when the best solution is improved
        if (sol.getObjectiveFunction() < bestSol.getObjectiveFunction()) {
            resetTabu();
        }
    }

    private void resetTabu() {
        iter = 0;
        for (int i = 0; i < sol.getNumBins(); i++) {
            for (int j = 0; j <= problem.getMaxItemWeight(); j++) {
                freq[i][j] = 0;
                tabu[i][j] = -1;
            }
        }
    }

    private boolean packSetIsBetter() {
        // Implement your condition for checking if packing the non-assigned items is better
        return true;
    }

    public List<Bin> packTrashcanIntoNewBins() {
        Bin bin1;
        Bin bin2;
        List<Item> tc = new ArrayList<>(problem.getTrashCan().getItemsList());

        Collections.sort(tc, Collections.reverseOrder());
        List<Item> itemsToRemove;

        int maxAttempts = 1000; // Maximum attempts to pack the items
        int attempts = 0;

        while (true) {
            boolean fullPacked = true;
            bin1 = new Bin(problem.getCapacity());
            bin2 = new Bin(problem.getCapacity());
            itemsToRemove = new ArrayList<>();

            for (Item item : tc) {
                if (bin1.addItem(item)) {
                    itemsToRemove.add(item);
                } else if (bin2.addItem(item)) {
                    itemsToRemove.add(item);
                } else {
                    fullPacked = false;
                }

                if (fullPacked) {
                    break;
                }
            }

            attempts++;
            if (fullPacked || attempts >= maxAttempts) {
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


}
