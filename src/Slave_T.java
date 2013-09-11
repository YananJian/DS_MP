import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.lang.reflect.*;

import common.*;

import java.io.*;


public class Slave_T {

    private String slave_id;
    private String manager_IP = common.Constants.IP_MASTER;
    private int manager_port = common.Constants.PORT_MASTER;
    private HashMap<String, MigratableProcess> runningpro = new HashMap<String, MigratableProcess>();
    private HashMap<String, MigratableProcess> suspendedpro = new HashMap<String, MigratableProcess>();
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
    	String class_name = cmd.split(" ")[0];
    	try {
    		MigratableProcess p;
			@SuppressWarnings("unchecked")
			Class<MigratableProcess> c = (Class<MigratableProcess>)Class.forName(class_name);
			Constructor<MigratableProcess> constructor = c.getConstructor(String[].class);
			String tmp = cmd.substring(cmd.indexOf(" ")).trim();
			if (tmp == "")
				p = constructor.newInstance();
			else
			{			
				Object params = tmp.split(" ");
				p = constructor.newInstance(params);
				this.runningpro.put(class_name, p);
				Thread t = new Thread(p);
				t.start();
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void record(MigratableProcess p, String cmd)
    {
    	TransactionalFileOutputStream fs = new TransactionalFileOutputStream(
    			         						"process_"+cmd+"_tmp");
    	ObjectOutputStream objectStream;

		try {
			objectStream = new ObjectOutputStream(fs);
			objectStream.writeObject(p);
			objectStream.close();
			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void suspend(String cmd)
    {
    	System.out.println("Suspended cmd:"+cmd);
    	MigratableProcess p = this.runningpro.remove(cmd);
    	
    	if (p == null)
    	{
    		System.out.println("Process "+cmd+" is not running!");
    		return;
    	}
    	this.suspendedpro.put(cmd, p);
    	this.record(p, cmd);
    	Msg m = new Msg("",cmd,""); // Should use pid as the last argument
    	m.set_status(Constants.Status.SUSPENDED);
    	m.set_migprocess(p);
    	try {
			this.writeToServer(m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Writing to Server Error");
			e.printStackTrace();
		}
    }

    public void terminate(String cmd)
    {
    	System.out.println("Terminated cmd:"+cmd);
    }
    
    public Constants.RESULT resume(String cmd, MigratableProcess p)
    {
    	if (p == null)
    	{
    		System.out.println("Process "+cmd+" has not been launched!"
    				           + "Please use L <cmd> <param1> <param2> ... to launch the process");
    		return Constants.RESULT.PROC_NOT_LAUNCHED;
    	}
    	else
    	{
    		Thread t = new Thread(p);
    		p.print();
    		t.start();
    		return Constants.RESULT.SUCCESS;
    	}
    }
    
    public void report_back(Msg m)
    {
    	System.out.println("Report status");
    }

    
    public void process(Msg msg) throws IOException {
	String act = msg.get_action();
	String cmd = msg.get_cmd(); // S:Suspend, L:launch, T:teminate, R:resume
	String to_ip = msg.get_toip();
	int to_port = msg.get_toport();
	MigratableProcess p = msg.get_migprocess();
	// cases
	if (act.equals("L"))
		launch(cmd);
	else if (act.equals("S"))
		suspend(cmd);
	else if (act.equals("T"))
		terminate(cmd);
	else if (act.equals("R"))
		resume(cmd, p);
    }

    public void writeToServer(Msg reply) throws IOException {
    	oos.writeObject(reply);
    	oos.flush();
    }
    
    public void read_server(Socket sock) throws IOException, ClassNotFoundException
    {
    	ois = new ObjectInputStream(sock.getInputStream());
    	while (true) { 
    	    Msg msg = (Msg) ois.readObject();
    	    //System.out.println("action:"+msg.get_action());
    	    String intended_slave_id = msg.get_sid();
    	    String pid = msg.get_pid();
    	    System.out.println("Slave id:"+intended_slave_id+" Process id:"+pid);  	    
    	    this.process(msg);  	    
    	}
    }
	
    public void connect() throws InterruptedException, ClassNotFoundException{
	try {
	    sock = new Socket(this.manager_IP, this.manager_port);	    
	    oos = new ObjectOutputStream(sock.getOutputStream());
	    Msg greeting = new Msg("", "","");
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
