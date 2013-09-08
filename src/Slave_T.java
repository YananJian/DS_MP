import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;

import common.Constants;
import common.Msg;

import java.io.*;


public class Slave_T {

    private String slave_id;
    private String manager_IP = "";
    private int manager_port = 0;
    private Constants.Status status;
	
    public void set_status(Constants.Status stat) {
	this.status = stat;
    }

    public Constants.Status get_status() {
	return this.status;
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

    public void process(ObjectOutputStream oos, Msg msg) throws IOException {
	String act = msg.get_action();
	String cmd = msg.get_cmd();
	String to_ip = msg.get_toip();
	int to_port = msg.get_toport();
	// cases

	// send reply to server
	this.set_status(Constants.Status.BUSY);
	Msg reply = new Msg("", "");

	reply.set_status(this.get_status()); 
	this.writeToServer(oos, reply);
	return;
    }

    public void writeToServer(ObjectOutputStream oos, Msg reply) throws IOException {
	oos.writeObject(reply);
    }

    public void readFromServer(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
	while (true) { 
	    Msg msg = (Msg) ois.readObject();
	    String intended_slave_id = msg.get_sid();
	    if (intended_slave_id.equals(slave_id)) {
		this.process(oos, msg);
	    }
	}
    }
	
    public void connect() throws InterruptedException, ClassNotFoundException{
	try {
	    Socket sock = new Socket(this.manager_IP, this.manager_port);	    

	    ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
	    ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
	    Msg greeting = new Msg("", "");
	    greeting.set_status(this.get_status()); 
	    this.writeToServer(oos, greeting);
	    this.readFromServer(ois, oos);	               
	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}	
    }
    
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
	// TODO Auto-generated method stub
	Slave_T slave = new Slave_T();
	slave.set_status(Constants.Status.IDLE);
	if (slave.argValidate(args)){
	    slave.manager_IP = args[1].split(":")[0];
	    slave.manager_port = Integer.parseInt(args[1].split(":")[1]);
	}
	slave.connect();
	
    }
    
}
