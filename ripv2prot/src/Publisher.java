import com.oracle.tools.packager.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Publisher implements Runnable {
    private int hostPort;
    private String neighbourIp;
    private ArrayList<Integer> portList;
    private RoutingTable routingTable;

    private TableService service;

    public Publisher(TableService service,int hostPort,int portIncrement, String neighbourIp,ArrayList<Integer> portList) {
        this.service = service;
        this.routingTable = service.getTable();
        this.neighbourIp = neighbourIp;
        this.portList = portList;
        this.hostPort = hostPort + portIncrement;
    }

    @Override
    public void run() {
        new Thread(() -> sendPackets()).run();
    }

    public void sendPackets() {
        try {
            DatagramSocket socket = new DatagramSocket(hostPort);

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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
        objectStream.writeObject(routingTable);
        byte[] data = outputStream.toByteArray();

        DatagramPacket packet = new DatagramPacket(data,data.length, InetAddress.getByName(neighbourIp),port);
        socket.send(packet);
    }
}
