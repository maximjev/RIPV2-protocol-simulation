import java.io.Serializable;

public class Message implements Serializable {
    private String network;
    private String message;

    public Message(String network, String message) {
        this.network = network;
        this.message = message;

    }
    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
