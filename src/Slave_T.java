import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;

import common.Constants;
import common.Msg;

import java.io.*;


public class Slave_T {

    private String slave_id;
    private String manager_IP = common.Constants.IP_MASTER;
    private int manager_port = common.Constants.PORT_MASTER;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Constants.Status status;
    private Socket sock = null;
	
    public void set_status(Constants.Status stat) {
	this.status = stat;
    }

    public Constants.Status get_status() {
    	return this.status;
    }

    public boolean argValidate(String [] args){
	if (args.length == 2){
	    if ((!args[0].equals("-c")) && (!args[1].contains(":"))){
		return false;
	    }
	    return true;
	}
	return false;
    }

    // TODO: implement
    
    public void launch(String cmd)
    {
    	System.out.println("Launched cmd:"+cmd);	
    }
    
    public void suspend(String cmd)
    {
    	System.out.println("Suspended cmd:"+cmd);	
    }

    public void terminate(String cmd)
    {
    	System.out.println("Terminated cmd:"+cmd);
    }
    public void resume(String cmd)
    {
    	System.out.println("Resumed cmd:"+cmd);
    }
    
    public void report_status()
    {
    	System.out.println("Report status");
    }
    
    public void process(Msg msg) throws IOException {
	String act = msg.get_action();
	String cmd = msg.get_cmd(); // S:Suspend, L: launch, T:teminate, R:resume
	String to_ip = msg.get_toip();
	int to_port = msg.get_toport();
	// cases
	if (act.equals("L"))
		launch(cmd);
	else if (act.equals("S"))
		suspend(cmd);
	else if (act.equals("T"))
		terminate(cmd);
	else if (act.equals("R"))
		resume(cmd);
	else
	{
		report_status();		
	}
	// send reply to server
	/*
	this.set_status(Constants.Status.BUSY);
	Msg reply = new Msg("", "");
	
	reply.set_status(this.get_status()); 
	this.writeToServer(reply);
	return;*/
    }

    public void writeToServer(Msg reply) throws IOException {
    	oos.writeObject(reply);
    	oos.flush();
    }

    public void readFromServer(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
	while (true) { 
	    Msg msg = (Msg) ois.readObject();
	    String intended_slave_id = msg.get_sid();
	    if (intended_slave_id.equals(this.slave_id)) {
		this.process(msg);
	    }
	}
    }
    
    public void read_server(Socket sock) throws IOException, ClassNotFoundException
    {
    	ois = new ObjectInputStream(sock.getInputStream());
    	while (true) { 
    	    Msg msg = (Msg) ois.readObject();
    	    //System.out.println("action:"+msg.get_action());
    	    String intended_slave_id = msg.get_sid();
    	    System.out.println("Slave id:"+intended_slave_id);  	    
    	    this.process(msg);
    	    
    	}
    }
	
    public void connect() throws InterruptedException, ClassNotFoundException{
	try {
	    sock = new Socket(this.manager_IP, this.manager_port);	    
	    oos = new ObjectOutputStream(sock.getOutputStream());
	    Msg greeting = new Msg("", "");
	    greeting.set_status(Constants.Status.IDLE); 
	    this.writeToServer(greeting);
	    this.read_server(sock);
	    //this.readFromServer(ois, oos);	               
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
