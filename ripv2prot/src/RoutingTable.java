import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class RoutingTable implements Serializable {
    private ArrayList<TableRow> rows;
    private HashMap<String,TableRow> rowsMap;
    private ArrayList<TableRow> timeoutedRows;

    public RoutingTable(){
        rows = new ArrayList<>();
        rowsMap = new HashMap<>();
    }

    public ArrayList<TableRow> get_rows() {
        return rows;
    }

    public HashMap<String, TableRow> getRowsMap() {
        return rowsMap;
    }

    public void addRow(TableRow row){
        rows.add(row);
        rowsMap.put(row.getAddress(),row);
    }
    public void removeRow(TableRow row) {
        rows.remove(row);
        rowsMap.remove(row.getAddress());
    }

    public void updateCost(String network,String nextHop, int cost){
        for (TableRow row : rows) {
            synchronized (row){
                if ((network.equals(row.getAddress()))) {
                    row.setCost(cost);
                    row.setNextHop(nextHop);
                    rowsMap.get(row.getAddress()).setCost(cost);
                    rowsMap.get(row.getAddress()).setNextHop(nextHop);
                }
            }
        }
    }
    public boolean isDown(String nextHop) {
        boolean hasEntry = false;
        for (TableRow row : rows) {
            synchronized (row) {
                if(row.getNextHop().equals(nextHop) && row.getCost() != 16) {
                    row.setCost(16);
                    rowsMap.get(row.getAddress()).setCost(16);
                    hasEntry = true;
                }
            }
        }
        return hasEntry;
    }

    public String getNextHopByNetwork(String network) {
        return rowsMap.get(network).getNextHop();
    }
}