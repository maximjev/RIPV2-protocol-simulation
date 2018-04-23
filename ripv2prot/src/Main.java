import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Main {
    public static void main(String args[]) {
        if(args.length != 1) {
            System.out.println("wrong argument count, enter config file name");
            System.exit(1);
        }
        parseFile(args[0]);
    }

    public static void parseFile(String path) {
        ArrayList<Integer> neighbourPorts = new ArrayList();
        HashMap<Integer,Integer> portPairs = new HashMap<>();
        ArrayList<String> myInterfaces = new ArrayList<>();

        RoutingTable table = new RoutingTable();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int myCost = 0;
            int sendingPort = 63100; // default
            String myNextHop = "0.0.0.0:0";
            String line;

            while((line = reader.readLine()) != null) {

                if(line.contains("LINK")) {
                    String[] adds = line.substring(line.indexOf(":")+ 2).split("-");
                    myInterfaces.add(adds[0]);

                    Logger.log(adds[0] + "  " + adds[1]);

                    int myPort = Integer.parseInt(adds[0].substring(adds[0].indexOf(":")+1));
                    int neighbourPort = Integer.parseInt(adds[1].substring(adds[1].indexOf(":")+1));
                    sendingPort = myPort;

                    portPairs.put(myPort,neighbourPort);
                    neighbourPorts.add(neighbourPort);

                    Logger.log(Integer.toString(myPort));
                }
                if(line.contains("NETWORK")) {
                    String network = line.substring(line.indexOf(":") + 2);
                    table.addRow(new TableRow(network, myNextHop, myCost));
                }
            }
            startRouter(table, portPairs, neighbourPorts, sendingPort, myNextHop, myInterfaces);

        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void startRouter(RoutingTable table,
                                   HashMap<Integer,Integer> portPairs,
                                   ArrayList<Integer> neighbourPorts,
                                   int sendingPort,
                                   String myIp,
                                   ArrayList<String> myInterfaces) {
        TableService service = new TableService(table, myIp, myInterfaces);
        String localIp = "127.0.0.1";
        try {
            for (Map.Entry<Integer,Integer> pair: portPairs.entrySet()) {
                new Thread(new Receiver(service, localIp, pair.getValue(),pair.getKey())).start();
            }
            new Thread(new Publisher(service, sendingPort, localIp, neighbourPorts)).start();

        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }
}
