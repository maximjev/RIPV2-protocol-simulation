import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Receiver implements Runnable {
    private DatagramSocket socket;
    private HashMap<InetSocketAddress, Long> times;

    private TableService service;

    public Receiver(TableService service, int myPort) throws SocketException {
        this.service = service;
        this.times = new HashMap<>();
        this.socket = new DatagramSocket(myPort);
    }

    public void checkTime() throws InterruptedException{
        Set<Map.Entry<InetSocketAddress,Long>> entrySet = times.entrySet();
        while(true) {
            Thread.sleep(5000);
            for(Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
                Map.Entry<InetSocketAddress,Long> entry = (Map.Entry<InetSocketAddress,Long>) iterator.next();
                if(System.currentTimeMillis() -  entry.getValue() > 6000) {
                    Logger.log("timeout of: "  + entry.getKey().getPort());
                    service.lostConnection(
                            entry.getKey().getAddress().getHostAddress(),
                            entry.getKey().getPort());
                    times.remove(entry);
                }
            }
        }
    }

    @Override
    public void run() {
        new Thread(this::receive).start();
        try {
            checkTime();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receive() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                long time = System.currentTimeMillis();
                times.put(new InetSocketAddress(packet.getAddress().getHostAddress(), packet.getPort()), time);

                buffer = packet.getData();

                ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(buffer));
                RoutingTable table = (RoutingTable) inputStream.readObject();
                Logger.log(String.format("Recieving packet from: %s",packet.getAddress().toString()));

                new Thread(() -> service.processTable(table,packet.getAddress().getHostAddress(),packet.getPort())).start();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }


}
