import java.util.ArrayList;
import java.util.HashMap;

public class TableService {
    private RoutingTable table;
    private String myIp;
    private int decrement;

    public TableService(RoutingTable table, String myIp, int portDecrement) {
        this.table = table;
        this.myIp = myIp;
        this.decrement = portDecrement;
        printTable();
    }

    public void processTable(RoutingTable neighbourTable, String neighbourIp,int neighbourPort) {
        boolean changes = false;
        ArrayList<TableRow> neighbourRows = neighbourTable.get_rows();
        HashMap<String,TableRow> myRowsMap = table.getRowsMap();
        TableRow newRow;
        neighbourPort = neighbourPort - decrement;

        String neighbour = neighbourIp + ":" + Integer.toString(neighbourPort);

        String neighbourAddress;
        int neighbourCost;

        for (TableRow neighbourRow : neighbourRows) {
            neighbourAddress = neighbourRow.getAddress();
            neighbourCost = neighbourRow.getCost();
            Logger.log("neighbourAddress: " + neighbourAddress);
            Logger.log("neighbourCost: " + neighbourCost);
            if(!myRowsMap.containsKey(neighbourAddress)) {
                newRow = new TableRow(
                        neighbourAddress,
                        neighbour,
                        neighbourCost +1);
                table.addRow(newRow);
                changes = true;
                Logger.log("new entry changes");

            }
            if(myRowsMap.containsKey(neighbourAddress) &&
                    myRowsMap.get(neighbourAddress).getCost() != 0) {
                if(neighbourCost != 16 &&
                        neighbourCost + 1 != myRowsMap.get(neighbourAddress).getCost() &&
                        neighbourCost < myRowsMap.get(neighbourAddress).getCost()) {
                    table.updateCost(neighbourAddress, neighbour, neighbourCost + 1);
                    changes = true;
                    Logger.log("update cost changes");
                    Logger.log("Network: " + neighbourAddress + " cost: " + neighbourCost);
                }

                if(myRowsMap.get(neighbourAddress).getCost() != 16 &&
                        myRowsMap.get(neighbourAddress).getCost() != 1 &&
                        neighbourCost == 16 &&
                        myRowsMap.get(neighbourAddress).getNextHop().equals(neighbour)) {
                    table.updateCost(neighbourAddress, neighbour, 16);
                    Logger.log("lost connection to " + neighbourAddress);
                    changes = true;
                }
            }

        }
        if(changes) {
            printTable();
        }
    }

    private void printTable() {
        Logger.print("===============================================");
        Logger.print("Network \t Next hop \t Cost");
        for(TableRow row : table.get_rows())
        {
            Logger.print(row.getAddress() + "\t " + row.getNextHop() + "\t " + row.getCost());
        }
    }

    public RoutingTable getTable() {
        return this.table;
    }

    public void lostConnection(String ip, int port) {
        port = port - decrement;
        String nextHop = ip + ":" + Integer.toString(port);
        Logger.log("timeout of nexthop: " + nextHop);
        if(table.isDown(nextHop)) {
            printTable();
        }
    }

    public void establishedConnection(String nextHop) {
        //if(table.isUp(nextHop)) {
          //  printTable();
        //}
    }

    public void clearTable() {
        for (TableRow row : table.get_rows()) {
            synchronized (row) {
                if (row.getCost() == 16) {
                    table.removeRow(row);
                }
            }
        }
    }
}
