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


    public void print() {
        for (Bin b : bins) {
            System.out.println("Bin");
            for (Item i : b.getItemsList()) {
                System.out.println(i.getWeight());
            }
        }
    }

    public void printNumBin() {
        System.out.println("Bin num: " + bins.size());
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

    public double getObjectiveFunction() {

        // Combine both objectives with the weighted sum
        double objectiveValue =  getTotalWastedCapacity() + problem.getTrashCan().getTotalWeight();


        return objectiveValue;
    }


    public boolean isSolutionComplete() {

       return problem.getTrashCan().getTotalItem() ==0;
    }


}
