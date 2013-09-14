import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.lang.reflect.*;

import common.*;

import java.io.*;


public class Slave {

    private String slave_id;
    private String manager_IP = common.Constants.IP_MASTER;
    private int manager_port = common.Constants.PORT_MASTER;
    private HashMap<String, MigratableProcess> runningpro = new HashMap<String, MigratableProcess>();
    private HashMap<String, MigratableProcess> suspendedpro = new HashMap<String, MigratableProcess>();
    private HashMap<String, Thread> runningthreads = new HashMap<String, Thread>();
    private HashMap<String, Thread> suspendedthreads = new HashMap<String, Thread>();
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
    
    public void launch(String cmd, String pid)
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
			}
			this.runningpro.put(pid, p);			
			Thread t = new Thread(p);
			t.start();		
			this.runningthreads.put(pid, t);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Can not find the process's .class file");
			//e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Can not find the process's method");
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
    
    public void record(MigratableProcess p, String pid)
    {
    	TransactionalFileOutputStream fs = new TransactionalFileOutputStream(
    			         						"./serialized/process_"+pid+"_tmp");
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
    
    @SuppressWarnings("deprecation")
	public void suspend(String cmd, String pid)
    {
    	System.out.println("Suspended cmd:"+pid);
    	MigratableProcess p = this.runningpro.remove(pid);
    	Thread t = this.runningthreads.remove(pid);
    	
    	if (p == null)
    	{
    		System.out.println("Process "+pid+" is not running!");
    		return;
    	}
    	this.suspendedpro.put(pid, p);
    	p.suspend();
    	t.suspend();
    	this.record(p, pid);
    	this.suspendedthreads.put(pid, t);
    	Msg m = new Msg("",cmd, pid); 
    	m.set_slaveid(this.slave_id);
    	m.set_status(Constants.Status.SUSPENDED);
    	
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
		System.out.println("Preparing to terminate");
    	MigratableProcess p = null;
    	p = runningpro.remove(cmd);
    	if (p == null)
    		p = suspendedpro.remove(cmd);
    	
    	if (p != null)
    	{
    		p.terminate();
    		System.out.println("Terminated");
    	}
    	Thread t = suspendedthreads.remove(cmd);
    	if (t != null)
    		t.interrupt();
    	else
    	{
    		t = this.runningthreads.remove(cmd);
    		if (t != null)
    			t.interrupt();
    	}
    	
    }
    
    public Constants.RESULT resume(String pid)
    {
    	String fname = "./serialized/process_"+pid+"_tmp";
    	
    	@SuppressWarnings("resource")
		TransactionalFileInputStream fis = new TransactionalFileInputStream(fname);
    	
    	MigratableProcess p;
		try {
			ObjectInputStream ois = new ObjectInputStream(fis);
			p = (MigratableProcess)ois.readObject();
			ois.close();
			fis.close();
			Thread t = new Thread(p);
	    	p.print();
	    	t.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Process "+pid+" has not been launched!"
			           + "Please use L <cmd> <param1> <param2> ... to launch the process");
			return Constants.RESULT.PROC_NOT_LAUNCHED;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	return Constants.RESULT.SUCCESS;
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
	String pid = msg.get_pid();
	
	// cases
	
	if (act.equals("L"))
		launch(cmd, pid);
	else if (act.equals("S"))
		suspend(cmd, pid);
	else if (act.equals("T"))
		terminate(pid);
	else if (act.equals("R"))
		resume(pid);
    }

    public void writeToServer(Msg reply) throws IOException {
    	oos.writeObject(reply);
    	oos.flush();
    }
    
    public void read_server() throws IOException, ClassNotFoundException
    {
    	ois = new ObjectInputStream(sock.getInputStream());
    	while (true) { 
    			Msg msg = (Msg) ois.readObject();
    			System.out.println("Got the msg");
    			//System.out.println("action:"+msg.get_action());   			
    			this.slave_id = msg.get_sid();
    			String pid = msg.get_pid();
    			System.out.println("\nSlave id:"+this.slave_id+" Process id:"+pid);  	    
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
	    this.read_server();
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
	Slave slave = new Slave();
	slave.set_status(Constants.Status.IDLE);
	if (slave.argValidate(args)){
	    slave.manager_IP = args[1].split(":")[0];
	    slave.manager_port = Integer.parseInt(args[1].split(":")[1]);
	}
	slave.connect();
	
    }
    
}
