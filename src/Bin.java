import java.util.ArrayList;
import java.util.List;
// Let B = {b1,b2,b3,b4 ... bm-2} be the set of currently utilized bins
// I_B ⊆ I be the set of items assigned to the bins in B
// I_b the set of items currently packed into bin b ∈ B.
// I_TC denote the set of items not currently assigned to any bin.
// w(S) be total weight  of a set of items S
// |S| be cardinality of a set of items S
// The total weight and number of items currently assigned to bin b ∈ (B ∪ TC) will be denoted by
// w(b) = w(I_b) and |b|=|I_b|.

public class Bin {
    public static int counter = 1;
    public Bin(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>();
        this.id = counter++;
    }

    private int capacity;

    private int remainingCapacity;

    private int id;

    private List<Item> items;

    public List<Item> getItemsList() {
        return this.items;
    }

    public void setItemsList(List<Item> items) {
        this.items = items;
    }

    public int getRemainingCapacity() {
        remainingCapacity = capacity - getTotalWeight();
        return remainingCapacity;
    }

    public void setUnusedSpace(int unusedSpace) {
        this.remainingCapacity = unusedSpace;
    }

    public int getTotalWeight() {
        int weight = 0;
        for (Item i : this.items) {
            weight += i.getWeight();
        }

        return weight;
    }

    public int getTotalItem(){
        return this.items.size();
    }

    public boolean addItem(Item item){
        if(getTotalWeight() + item.getWeight() > capacity){
            return false;
        }else {
            item.setInBin(true);
            item.setInTrashCan(false);
            items.add(item);
            return true;
        }
    }

    public void removeItem(Item item){
        item.setInBin(false);
        item.setInTrashCan(true);
        items.remove(item);
    }

    public boolean hasExcessWeightItems() {
        int count = 0;

        for (Item item : items) {
            if (item.getWeight() > capacity) {
                // If item weight exceeds capacity, increment count
                count++;
                if (count > 2) {
                    return true; // If count exceeds 2, return true
                }
            }
        }

        return false; // If count <= 2, return false
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
