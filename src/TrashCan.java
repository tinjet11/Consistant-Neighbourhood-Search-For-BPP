import java.util.ArrayList;
import java.util.List;

public class TrashCan{
    private int capacity;
    public TrashCan(int capacity){
        this.items = new ArrayList<>();
        this.capacity =capacity;
    }
    private List<Item> items;

    public List<Item> getItemsList() {
        return this.items;
    }

    public void setItemsList(List<Item> items) {
        this.items = items;
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

//        if(hasExcessWeightItems()){
//        return false;
//        }else{
            item.setInTrashCan(true);
            item.setInBin(false);
            items.add(item);

            return true;
//        }

    }

    public void print() {

            for (Item i : getItemsList()) {
                System.out.print(i.getWeight()+ ", ");
            }

    }

    public boolean hasExcessWeightItems() {
        int count = 0;

        for (Item item : items) {
            if (item.getWeight() > capacity/2) {
                // If item weight exceeds capacity, increment count
                count++;
                if (count > 2) {
                    return true; // If count exceeds 2, return true
                }
            }
        }

        return false; // If count <= 2, return false
    }

    public void removeItem(Item item){
        item.setInTrashCan(false);
        item.setInBin(true);
        items.remove(item);
    }

}
