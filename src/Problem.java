import java.util.List;

public  class Problem {

    private List<Item> allItems;

    private TrashCan trashCan;

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    private String problemName;

    private int capacity;

    public Problem(List<Item> allItems,int capacity,String problemName){
        this.allItems = allItems;
        this.capacity = capacity;
        this.problemName = problemName;
        trashCan = new TrashCan(capacity);
    }


    public List<Item> getAllItems() {
        return allItems;
    }

    public void setAllItems(List<Item> allItems) {
        this.allItems = allItems;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public TrashCan getTrashCan() {
        return trashCan;
    }

    public void setTrashCan(TrashCan trashCan) {
        this.trashCan = trashCan;
    }

    public int getObjectiveFunctionValue(){
        int totalWeight = 0;
        for(Item i : getAllItems()){
            if(i.isInTrashCan()){
                totalWeight += i.getWeight();
            }
        }

        int obj = getAllItems().size() * totalWeight - getTrashCan().getItemsList().size();

        return obj;
    }


    public int getMaxItemWeight() {
        List<Item> items = this.allItems;
        if (items == null || items.isEmpty()) {
            return 0;
        }

        Item maxWeightItem = items.get(0); // Assume the first item has the maximum weight initially

        for (int i = 1; i < items.size(); i++) {
            Item currentItem = items.get(i);
            if (currentItem.getWeight() > maxWeightItem.getWeight()) {
                maxWeightItem = currentItem;
            }
        }

        return maxWeightItem.getWeight();
    }
}
