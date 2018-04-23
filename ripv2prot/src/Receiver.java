import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Receiver implements Runnable {
    private String neighbourIp;
    private int neighbourPort;
    private DatagramSocket socket;
    private long time;

    private TableService service;

    public Receiver(TableService service,String neighbourIp,int neighbourPort, int myPort) throws SocketException {
        this.service = service;
        this.neighbourIp = neighbourIp;
        this.neighbourPort = neighbourPort;
        this.socket = new DatagramSocket(myPort);
    }

    public void checkTime() throws InterruptedException{
        while(true) {
            Thread.sleep(5000);
            if(System.currentTimeMillis() - time > 6000) {
                Logger.log("lost connection to: " + neighbourIp + ":" + neighbourPort);
                service.lostConnection(this.neighbourIp + ":" + neighbourPort);
            } else {
                service.establishedConnection(this.neighbourIp + ":" + neighbourPort);
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
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                socket.receive(packet);
                time = System.currentTimeMillis();
                buffer = packet.getData();

                ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(buffer));
                RoutingTable table = (RoutingTable) inputStream.readObject();
                Logger.log(String.format("Recieving packet from: %s",packet.getAddress().toString()));
                service.processTable(table,neighbourIp,neighbourPort);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }


}
