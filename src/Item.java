public class Item implements Comparable<Item> {

    private int weight;

    private boolean inTrashCan = false;
    private boolean inBin= false;

    public Item(int weight) {
        this.weight = weight;
    }


    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }


    public boolean isInTrashCan() {
        return inTrashCan;
    }

    public void setInTrashCan(boolean inTrashCan) {
        this.inTrashCan = inTrashCan;
    }


    public boolean isInBin() {
        return inBin;
    }

    public void setInBin(boolean inBin) {
        this.inBin = inBin;
    }

    @Override
    public String toString() {
        return "Item weight: " + weight;
    }

    @Override
    public int compareTo(Item other) {
        // Compare Items based on their weight
        return Integer.compare(this.weight, other.weight);
    }
}
