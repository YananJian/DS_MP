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
import common.Msg;

public class ProcessManager implements Runnable{
	
	private HashMap<String, String> sid_ipport = new HashMap<String, String>();
	private HashMap<String, String> sid_status = new HashMap<String, String>();
	private static ProcessManager pm = null;
	public ConcurrentLinkedQueue<Msg> msgQueue = new ConcurrentLinkedQueue<Msg>();
	public ConcurrentLinkedQueue<String> ideal_ipport = new ConcurrentLinkedQueue<String>();
	MsgProcessor mp = MsgProcessor.getInstance();
	
	public static synchronized ProcessManager getInstance()
	{
		if (pm == null)
			pm = new ProcessManager();
		return pm;
	}

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
			md5 = new BigInteger(1, mdEnc.digest()).toString(16);	
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {return md5;}
		
	}
	
	private void dispatchMsg(Msg msg)
	{
		String ip_port = this.ideal_ipport.poll();
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProcessManager pm = ProcessManager.getInstance();
		if (!pm.argValidate(args)){
			System.out.println("Argument Error");
			return;
		}
		Thread mt = new Thread(pm.mp);
		mt.start();
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Msg msg = this.msgQueue.poll();
		System.out.format("cmds:%s, action: %s", 
				           msg.get_cmd(), msg.get_action());
	}

	
}

