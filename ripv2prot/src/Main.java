import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;


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

        RoutingTable table = new RoutingTable();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            int myCost = 0;
            int myPort = 53000; // default
            String myNextHop = "0.0.0.0:0";
            String line;

            while((line = reader.readLine()) != null) {

                if(line.contains("LINK")) {
                    String[] adds = line.substring(line.indexOf(":")+ 2).split("-");

                    Logger.log(adds[0] + "  " + adds[1]);

                     myPort = Integer.parseInt(adds[0].substring(adds[0].indexOf(":")+1));
                    int neighbourPort = Integer.parseInt(adds[1].substring(adds[1].indexOf(":")+1));

                    neighbourPorts.add(neighbourPort);

                    Logger.log(Integer.toString(myPort));
                }
                if(line.contains("NETWORK")) {
                    String network = line.substring(line.indexOf(":") + 2);
                    table.addRow(new TableRow(network, myNextHop, myCost));
                }
            }
            startRouter(table, neighbourPorts, myNextHop, myPort);

        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void startRouter(
            RoutingTable table,
            ArrayList<Integer> neighbourPorts,
            String myIp,
            int myPort) {

        String localIp = "127.0.0.1";
        int increment = 1000;
        String myExternalIp = localIp + ":" + Integer.toString(myPort);
        TableService service = new TableService(table, myExternalIp, increment);

        try {
            new Thread(new Receiver(service,myPort)).start();
            new Thread(new Publisher(service, myPort, increment, localIp, neighbourPorts)).start();

            while(true) {
                try {
                    Logger.print("enter network to deliver and your message");
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String input = br.readLine();
                    service.saveMessage(processInput(input));
                } catch (StringIndexOutOfBoundsException | NullPointerException | NumberFormatException ex)  {
                    Logger.print("Incorrect input format");
                    continue;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private static Message processInput(String input) {
        String network = input.substring(0,input.indexOf(" "));
        String message = input.substring(input.indexOf(" ") + 1);
        return new Message(network, message);
    }

}
