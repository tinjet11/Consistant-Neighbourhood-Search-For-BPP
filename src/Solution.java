import java.util.ArrayList;
import java.util.List;

public class Solution {
    Problem problem;

    List<Bin> bins;

    public List<Bin> getBins() {
        return this.bins;
    }

    public int getNumBins() {
        return this.bins.size();
    }


    public Solution(Problem problem, List<Bin> bins) {
        this.problem = problem;
        this.bins = bins;
    }

    public Bin getBinById(int id) {
        for (Bin bin : bins) {
            if (bin.getId() == id) {
                return bin;
            }
        }
        return null; // Return null if bin with given ID is not found
    }
    public Solution copy() {
        // Create a new Solution object with the same problem
        Solution newSolution = new Solution(this.problem, new ArrayList<>());

        // Create deep copies of all bins and their items
        for (Bin bin : this.bins) {
            Bin newBin = new Bin(problem.getCapacity());
            List<Item> newItems = new ArrayList<>();
            for (Item item : bin.getItemsList()) {
                Item newItem = new Item(item.getWeight());
                newItem.setInBin(item.isInBin());
                newItem.setInTrashCan(item.isInTrashCan());
                newItems.add(newItem);
            }
            newBin.setItemsList(newItems);
            newSolution.getBins().add(bin);
        }

        return newSolution;
    }

    public int getTotalWastedCapacity() {
        int total = 0;
        for (Bin bins : bins) {
            total += bins.getRemainingCapacity();
        }

        return total;
    }

    public double getObjectiveFunctionValue() {
        double x = 0;
        for (Bin bin : bins) {
            x += Math.pow(bin.getTotalWeight() / (double) problem.getCapacity(), 2);
        }

        double objectiveValue = 1 - (x / this.getNumBins());

        // Check for NaN and return 0 if NaN
        if (Double.isNaN(objectiveValue)) {
            return 0.0;
        }

        return objectiveValue;
    }


    public boolean isSolutionComplete() {
       return (problem.getTrashCan().getTotalItem() ==0);
    }


}
