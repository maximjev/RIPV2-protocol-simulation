import com.oracle.tools.packager.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Publisher implements Runnable {
    private int hostPort;
    private String neighbourIp;
    private ArrayList<Integer> portList;
    private RoutingTable routingTable;

    private DatagramSocket socket;
    private TableService service;

    public Publisher(TableService service,int hostPort,int portIncrement, String neighbourIp,ArrayList<Integer> portList) {
        this.service = service;
        this.routingTable = service.getTable();
        this.neighbourIp = neighbourIp;
        this.portList = portList;
        this.hostPort = hostPort + portIncrement;
        try {
            socket = new DatagramSocket(this.hostPort);
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        new Thread(() -> sendPackets()).start();
        new Thread(() -> forwardMessage()).start();
    }

    private void forwardMessage() {
        try {
            Message message;
            while (true) {
                Thread.sleep(3000);
                message = service.fetchMessage();
                if(message != null) {
                    sendMessage(message);
                    message = null;
                }
            }
        } catch(InterruptedException ex) {
            Logger.print("sleep interrupted");
        }
    }

    public void sendPackets() {
        try {
            Logger.log(String.format("Opening socket for sending with port: %d", hostPort));

            while (true) {
                Thread.sleep(3000);
                this.routingTable = service.getTable();
                for(int i = 0; i <portList.size(); i++) {
                    sendPacket(socket,portList.get(i));
                }

            }
        } catch(IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    public void sendPacket(DatagramSocket socket,Integer port) throws IOException {
        Logger.log(String.format("Sending packet to port: %d and ip: %s" ,port,neighbourIp));

        byte[] data = serialize(routingTable);

        DatagramPacket packet = new DatagramPacket(data,data.length, InetAddress.getByName(neighbourIp),port);
        socket.send(packet);
    }
    public void sendMessage(Message message) {
        try {
            if(message == null) {
                return;
            }
            String nextHop = service.getTable().getNextHopByNetwork(message.getNetwork());
            if (nextHop == null) {
                Logger.print("no nextHop found");
                return;
            }
            if (nextHop == "0.0.0.0:0") {
                service.printMessage(message);
                return;
            }
            service.indicateMessageForwarding(message.getNetwork());

            String nextHopIp = nextHop.substring(0, nextHop.indexOf(":"));
            int nextHopPort = Integer.parseInt(nextHop.substring(nextHop.indexOf(":") + 1));

            byte[] data = serialize(message);
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(nextHopIp), nextHopPort);
            socket.send(packet);
        } catch(IOException ex) {
            Logger.print("unknown host");
            ex.printStackTrace();
        }

    }

    private byte[] serialize(Serializable obj) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);

            objectStream.writeObject(obj);
            return outputStream.toByteArray();
    }
}
