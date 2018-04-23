import java.io.Serializable;

public class TableRow implements Serializable {
    private String address;
    private String nextHop;
    private int cost;

    public TableRow(){}

    public TableRow(String address,String nextHop,int cost)
    {
        this.address = address;
        this.nextHop = nextHop;
        this.cost = cost;
    }

    public String getAddress() {
        return address;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) { this.nextHop = nextHop; }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}