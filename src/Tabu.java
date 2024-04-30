import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tabu {
    private Solution bestSol;
    private Solution sol;
    private int iter;
    private final int maxNmbIters;
    private final double timeLimitTabu;
    private final Problem problem;
    private final int[][] freq;
    private final int[][] tabu;

    public Tabu(Solution initSol, int maxNmbIters, Problem problem) {
        this.iter = 0;
        this.maxNmbIters = maxNmbIters;
        this.timeLimitTabu = 1;
        this.problem = problem;

        // Initialize freq and tabu arrays
        freq = new int[initSol.getNumBins()+10000][problem.getMaxItemWeight() + 1];
        tabu = new int[initSol.getNumBins()+10000][problem.getMaxItemWeight() + 1];
    }

    public Solution tabuSearch(Solution solution) {
        this.sol = solution.copy();
        this.bestSol = solution.copy();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (long) (timeLimitTabu * 1000);

        List<List<Item>> subsetsS;
        List<List<Item>> subsetsT;
        iter = 0;
        resetTabu();
        while (iter < maxNmbIters && System.currentTimeMillis() < endTime) {
            List<Item> SStar = new ArrayList<>();
            List<Item> TStar = new ArrayList<>();
            Bin bStar = null;
            int bStarId = -1;
            int minDelta = sol.getNumBins() * CNS_BP.weight(problem.getAllItems());

            for (Bin bin : sol.getBins()) {
                subsetsS = generateSubsets(bin.getItemsList());
                subsetsT = generateSubsets(problem.getTrashCan().getItemsList());

                for (List<Item> S : subsetsS) {
                    if (!isTabu(bin, S)) {
                        for (List<Item> T : subsetsT) {
                            if (isAllowedPair(S, T)) {
                                int totalWeightBin = bin.getTotalWeight();
                                int weightS = CNS_BP.weight(S);
                                int weightT = CNS_BP.weight(T);

                                // S = item to be moved to trashcan
                                // T = item to be moved to bin
                                if (totalWeightBin + weightT - weightS <= problem.getCapacity()) {
                                    int delta = sol.getNumBins() * (weightS - weightT) - (S.size() - T.size());
                                    if (delta <= minDelta) {
                                        minDelta = delta;
                                        SStar = S;
                                        TStar = T;
                                        bStar = bin;
                                        bStarId = bin.getId();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (TStar.isEmpty()) {
                CNSBP_Runner.converged = true;
                break; // Terminate when no allowed move exists
            }

            //System.out.println("Before swap: " + problem.getTrashCan().getTotalWeight());
            Bin newBin = performSwap(SStar, TStar, bStar);
            //System.out.println("After swap: " + problem.getTrashCan().getTotalWeight());
            sol.getBins().remove(sol.getBinById(bStarId));

            sol.getBins().add(newBin);

            if (sol.getObjectiveFunctionValue() < bestSol.getObjectiveFunctionValue()) {
                bestSol = sol.copy();
                resetTabu();
            }

            CNS_BP.PStar = new ArrayList<>();
            if (problem.getTrashCan().getTotalWeight() <= 2 * problem.getCapacity()
                    &&  (CNS_BP.weight(CNS_BP.PackSet(problem.getTrashCan().getItemsList(),new ArrayList<>())) >= CNS_BP.weight(problem.getTrashCan().getItemsList()) - problem.getCapacity()))
            {
                packTrashcanIntoNewBins();
                break; // Terminate when conditions met
            }

            updateTabu(bStar, SStar);

            iter++;
        }

        if (bestSol != sol) {
            return bestSol;
        } else {
            return sol;
        }
    }

    private List<List<Item>> generateSubsets(List<Item> items) {
        int maxSize = 3; // Maximum subset size
        int maxSubsets = 5000; // Maximum number of subsets to generate
        List<List<Item>> subsets = new ArrayList<>();
        int n = items.size();
        Collections.shuffle(items);

        // Adjusted iteration limit based on the number of items
        int numIterations = Math.min(maxSubsets, 1 << n);

        for (int i = 0; i < numIterations; i++) {
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
        int weightS = CNS_BP.weight(S);
        int weightT = CNS_BP.weight(T);
        return weightT > weightS ;//&& S.size() <= T.size() ; // Adjust as needed for allowed pairs
    }


    private Bin performSwap(List<Item> SStar, List<Item> TStar, Bin bStar) {

        List<Item> itemsToRemoveFromTC = new ArrayList<>();
        List<Item> itemsToAddToTC = new ArrayList<>();

        // S = item to be moved to trashcan
        // T = item to be moved to bin
        for (Item item : SStar) {
            itemsToAddToTC.add(item);
            bStar.removeItem(item);
        }
        // S = item to be moved to trashcan
        // T = item to be moved to bin
        for (Item item : TStar) {
           // if (!SStar.contains(item)) {
                itemsToRemoveFromTC.add(item);
                bStar.addItem(item);
         // }
        }

        // Remove the marked items from the trash can
        for (Item item : itemsToRemoveFromTC) {
            problem.getTrashCan().removeItem(item);
        }

//        System.out.println("Remove from trashcan " + CNS_BP.weight(itemsToRemoveFromTC));
        // Add items back to bin if needed
        for (Item item : itemsToAddToTC) {
            problem.getTrashCan().addItem(item);
        }

//        System.out.println("Add to trashcan " + CNS_BP.weight(itemsToAddToTC));
        return bStar;
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
        if (sol.getObjectiveFunctionValue() < bestSol.getObjectiveFunctionValue()) {
            bestSol = sol.copy();
            resetTabu();
        } else {
            for (Item item : S) {
                freq[bin.getId()][item.getWeight()]++;
                tabu[bin.getId()][item.getWeight()] = iter + freq[bin.getId()][item.getWeight()] / 2;
            }
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
//        Bin bin1;
//        Bin bin2;
//        List<Item> tc = new ArrayList<>(problem.getTrashCan().getItemsList());
//
//        Collections.sort(tc, Collections.reverseOrder());
//        List<Item> itemsToRemove;
//
//        int maxAttempts = 1000; // Maximum attempts to pack the items
//        int attempts = 0;
//
//        while (true) {
//            System.out.println("Repack to bin in tabu");
//            boolean fullPacked = true;
//            bin1 = new Bin(problem.getCapacity());
//            bin2 = new Bin(problem.getCapacity());
//            itemsToRemove = new ArrayList<>();
//
//            for (Item item : tc) {
//                if (bin1.addItem(item)) {
//                    itemsToRemove.add(item);
//                } else if (bin2.addItem(item)) {
//                    itemsToRemove.add(item);
//                } else {
//                    fullPacked = false;
//                }
//
//                if (fullPacked) {
//                    break;
//                }
//            }
//
//            attempts++;
//            if (fullPacked || attempts >= maxAttempts) {
//                break;
//            }
//
//            Collections.shuffle(tc);
//        }
//
//        // Remove the marked items from the trash can
//        for (Item item : itemsToRemove) {
//            problem.getTrashCan().removeItem(item);
//        }
//
//        List<Bin> newBins = new ArrayList<>();
//        newBins.add(bin1);
//        newBins.add(bin2);
        Solution nonAssigned = CNS_BP.firstFitDecreasing(problem.getTrashCan().getItemsList());

        List<Bin> newBins = new ArrayList<>();
        newBins.addAll(nonAssigned.getBins());
        return newBins;
    }


}