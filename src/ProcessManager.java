import java.util.*;
import java.nio.channels.*;
import java.math.*;
import java.security.*;
import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.io.FileInputStream;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.Serializable;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.*;

import common.Constants;
import common.Msg;

public class ProcessManager{
	
	private HashMap<String, String> sid_ipport = new HashMap<String, String>();
	//private HashMap<String, Constants.Status> sid_status = new HashMap<String, Constants.Status>();
	
	private static ProcessManager pm = new ProcessManager();
	
	public ConcurrentLinkedQueue<Msg> msgQueue = new ConcurrentLinkedQueue<Msg>();
	public ConcurrentLinkedQueue<String> sids = new ConcurrentLinkedQueue<String>();
	MsgProcessor mp = MsgProcessor.getInstance();
	CmdProcessor cp = CmdProcessor.getInstance();
	
	private ProcessManager(){}
	public static synchronized ProcessManager getInstance()
	{
		return pm;
	}
	/*
	public void set_sid_status(String sid, Constants.Status status)
	{
		this.sid_status.put(sid, status);
		
	}*/
	
	public boolean argValidate(String [] args){
		return true;
	}
	
	public void add_sidmap(String sid, String ipport)
	{
		this.sid_ipport.put(sid, ipport);
	}
	
	public String get_sidmap(String sid)
	{
		return this.sid_ipport.get(sid);
	}
	
	public String gen_slaveid(String ip, int port){
		MessageDigest mdEnc;
		String md5 = null;
		try {
			mdEnc = MessageDigest.getInstance("MD5");
			String cli_info = ip + String.valueOf(port);
			mdEnc.update(cli_info.getBytes(), 0, cli_info.length());
			md5 = mdEnc.digest().toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {return md5;}
		
	}
	
	public void dispatchMsg(Msg msg)
	{
		String sid = this.sids.peek();
		String ip_port = this.sid_ipport.get(sid);
		if (sid == null)
		{
			System.out.println("No ideal slaves");
			return;
		}
		if (ip_port == null)
		{
			System.out.println("No slaves");
			return;
		}
		msg.set_slaveid(sid);
		msg.set_to_ip(ip_port.split(":")[0]);
		msg.set_to_port(Integer.parseInt(ip_port.split(":")[1]));
		System.out.format("Dispatching Msg, ip:%s, port:%d", msg.get_toip(), msg.get_toport());
		mp.send2slave(msg);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProcessManager _pm = ProcessManager.getInstance();
		if (!_pm.argValidate(args)){
			System.out.println("Argument Error");
			return;
		}
		Thread mt = new Thread(_pm.mp);
		Thread ct = new Thread(_pm.cp);
		
		mt.start();
		ct.start();
		
	}

	/*
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true)
		{
			Msg msg = this.msgQueue.poll();
			System.out.format("cmds:%s, action: %s", 
				           		msg.get_cmd(), msg.get_action());
			this.dispatchMsg(msg);
		}
	}
	 */
	
}

