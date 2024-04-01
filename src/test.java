//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class test {
//
//    public static void main(String[] args) {
//        // Create a list of items in the trash can
//        List<Item> itemsInTrashCan = new ArrayList<>();
//        itemsInTrashCan.add(new Item(5));  // Item ID: 0, Weight: 5
//        itemsInTrashCan.add(new Item(8));  // Item ID: 1, Weight: 8
//        itemsInTrashCan.add(new Item(3));  // Item ID: 2, Weight: 3
//        itemsInTrashCan.add(new Item(10)); // Item ID: 3, Weight: 10
//        itemsInTrashCan.add(new Item(4));  // Item ID: 4, Weight: 4
//
//        // Create a Problem instance with the trash can items and capacity
//        Problem problem = new Problem(itemsInTrashCan, 15); // Capacity: 15
//
//
//        // Pack trash can items into new bins
//        List<Bin> newb = packTrashcanIntoNewBins1(problem);
//
//    }
//
//    public Bin rearrangeItemsBetweenBinAndTrashCan(Solution solution, int binIndex) {
//        Solution newSolution = solution.copy();
//        Bin updatedBin = newSolution.getBins().get(binIndex);// Create a copy of the original bin
//        int binCapacity = problem.getCapacity();
//
//        // Sort the items in the trash can in descending order of weight
//        List<Item> trashCanItems = problem.getTrashCan().getItemsList();
//        Collections.sort(trashCanItems, Collections.reverseOrder());
//
//        int countHeavyItems = 0; // Count of heavy items (weight > capacity/2) in trash can
//
//        // Iterate through the items in the trash can
//        for (Item item : trashCanItems) {
//            if (item.getWeight() > binCapacity / 2 && problem.getTrashCan().hasExcessWeightItems()) {
//                // Skip this item if it's heavy and we already have 2 such items
//                continue;
//            }
//
//            if (updatedBin.addItem(item)) {
//                // If the item was added to the bin, remove it from the trash can
//                problem.getTrashCan().removeItem(item);
//                System.out.println(item.isInBin());
//            }
//        }
//
//        return updatedBin;
//    }
//
//}
