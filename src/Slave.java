import java.io.*;
import java.net.*;
import Message.java // TODO: syntax??
import Process.java // TODO: syntax??

public class Slave {

    int slave_id;    

    public static void set_slave_id(int id) {
	slave_id = id;
    }

    public static void main(String[] args) throws IOException {
	
	try {
	    // listen to socket
	    socket = new Socket("localhost", 2000); // TODO: which socket?
	    ois = new ObjectInputStream(socket.getInputStream());

	    while (true) {
		// parse messages
		Message msg = (Message) ois.readObject(); 
		int intended_slave_id = msg.slave_id; 

		// if message intended for this slave: 
		if (intended_slave_id == slave_id) {
		    // finish parsing
		    int proc_id = msg.proc_id;
		    char act = msg.act;
		    char cmd = msg.cmd;
		    // attempt to process
		    Process process = new Process(proc_id, act, cmd); 
		}
	    }
	} 
	catch (Exception e) { e.printStackTrace(); }

    }

}