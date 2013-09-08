import java.io.IOException;
import java.net.*;

public class Slave_T {

    int slave_id;
    String manager_IP = "";
    int manager_port = 0;
    
	
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
    public void process(Message msg) {
	return;
    }
	
    public void connect(){
	try {
	    Socket sock = new Socket(this.manager_IP, this.manager_port);
	    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
	    while (true) { 
		Message msg = (Message) ois.readObject();
                int intended_slave_id = msg.slave_id;
                if (intended_slave_id == slave_id) {
		    slave.process(msg);
		}
	    }
	    
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
	if (slave.argValidate(args)){
	    slave.manager_IP = args[1].split(":")[0];
	    slave.manager_port = Integer.parseInt(args[1].split(":")[1]);
	}
	slave.connect();
    }
    
}
