import java.io.IOException;
import java.net.*;
import common.Constants;

public class Slave_T {

    private String slave_id;
    private String manager_IP = "";
    private int manager_port = 0;
    private Constants.status status;
	
    public void set_status(Constants.status stat) {
	self.status = stat;
    }

    public Constants.status get_status() {
	return self.status;
    }

    public boolean argValidate(String [] args){
	if (args.length == 2){
	    if ((!args[0].equals("-c")) && (args[1].contains(":"))){
		return false;
	    }
	    return true;
	}
	return false;
    }

    // TODO: implement
    public void process(Msg msg) {
	String act = msg.get_action();
	String cmd = msg.get_cmd();
	String to_id = msg.get_toid();
	String to_ip = msg.get_toip();
	int to_port = msg.get_toport();
	return;
    }

    public void writeToServer(Socket sock) {
	Msg msg = new Msg("", "");
	msg.set_status(slave.get_status()); 
	ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
	oos.writeObject(msg);
    }

    public void readFromServer(Socket sock) {
	ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
	while (true) { 
	    Msg msg = (Msg) ois.readObject();
	    String intended_slave_id = msg.slave_id;
	    if (intended_slave_id.equals(slave_id)) {
		slave.process(msg);
	    }
	}
    }
	
    public void connect(){
	try {
	    Socket sock = new Socket(this.manager_IP, this.manager_port);	    
	    slave.writeToServer(sock);
	    slave.readFromServer(sock);	               
	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}	
    }
    
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	Slave_T slave = new Slave_T();
	slave.set_status(IDLE);
	if (slave.argValidate(args)){
	    slave.manager_IP = args[1].split(":")[0];
	    slave.manager_port = Integer.parseInt(args[1].split(":")[1]);
	}
	slave.connect();
    }
    
}