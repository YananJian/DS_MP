import java.io.*;
import java.net.*;

public class Slave {

    int slave_id;
    int manager_IP;

    public void set_slave_id(int id) {
        this.slave_id = id;
    }

    public void set_manager_IP(int ip) {
        this.manager_IP = ip;
    }

    public static void main(String[] args) throws IOException {

	slave_id = args[0];
	manager_IP = args[1];

        try {
            // listen to socket
            Socket socket = new Socket("localhost", 4000); // TODO: what port should I use?
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

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
                    //attempt to process
                    MigratableProcess process = new MigratableProcess();
		    // TODO: depending on the action, do stuff; report result back to manager

		    // Case 1a: start thread from scratch; report success/failure
		    // Case 1b: start thread given process environment; report success/failure

		    // Case 2: kill thread; report success/failure

		    // Case 3: suspend thread; report success/failure

		    // Case 4: suspend thread and report process environment (new message class); report success/failure

		    // Case 5: resume existing thread given process environment; report success/failure
                }
            }
        }
        catch (Exception e) { e.printStackTrace(); }

    }

}