import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.IOException;
import java.util.concurrent.*;

import common.Constants;
import common.MigratableProcess;
import common.Constants.RESULT;
import common.Msg;

public class ProcessManager{
	
	HashMap<String, String> sid_ipport = new HashMap<String, String>();
	HashMap<String, String> ipport_sid = new HashMap<String, String>();
	HashMap<String, String> pid_cmd = new HashMap<String, String>();
	
	public ConcurrentLinkedQueue<String> sids = new ConcurrentLinkedQueue<String>();
	public HashMap<String, String>running_procs = new HashMap<String, String>();
	public HashMap<String, String> suspended_procs = new HashMap<String, String>();
	public HashMap<String, String> resumed_procs = new HashMap<String, String>();
	
	MsgProcessor mp = MsgProcessor.getInstance();
	CmdProcessor cp = CmdProcessor.getInstance();
	
	private static ProcessManager pm = new ProcessManager();
	
	private ProcessManager(){}
	public static synchronized ProcessManager getInstance()
	{
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
		String cli_info = ip + String.valueOf(port);
		return md5(cli_info);
	}
	
	public String md5(String str)
	{
		MessageDigest mdEnc;
		String md5 = null;
		try {
			mdEnc = MessageDigest.getInstance("MD5");			
			mdEnc.update(str.getBytes(), 0, str.length());
			md5 = mdEnc.digest().toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {return md5;}
	}
	
	// generate process id, make pid the identifier of processes, 
	//in case two processes with the same Class running at the same time
	public String gen_pid(String cmd)
	{
		//String pid = String.valueOf(System.currentTimeMillis());
		String pid = md5(cmd);
		return pid;	
	}
	
	
	public void dispatchMsg(Msg msg)
	{
		String sid = this.sids.peek();
		
		String ip_port = this.sid_ipport.get(sid);		
		if (sid == null)
		{
			System.out.println("Error: No available slaves");
			return;
		}
		if (ip_port == null)
		{
			System.out.println("Error: No slaves");
			return;
		}
		String ip = ip_port.split(":")[0];
		int port = Integer.parseInt(ip_port.split(":")[1]);
		
		if (msg.get_action().equals("L"))
		{
			String pid = gen_pid(msg.get_cmd());
			msg.set_pid(pid);
			System.out.println("Cmd+"+msg.get_cmd()+" pid:"+pid);
			pid_cmd.put(pid, msg.get_cmd());
			ipport_sid.put(ip_port, sid);
			if (running_procs.containsKey(pid))
			{
				System.out.println("pid:"+pid+" is running, please launch another one");
				return;
			}
			running_procs.put(pid, sid);
			System.out.println("*************");
			System.out.println("Running Process:"+pid);		
			System.out.println("*************");
		}
		else if (msg.get_action().equals("S"))
		{
			sid = running_procs.remove(msg.get_cmd());
			if (sid == null)
			{
				sid = resumed_procs.remove(msg.get_cmd());
				if (sid == null)
				{
					System.out.println("Error: pid "+msg.get_cmd()+" has not been launched");
					return;
				}
			}
			msg.set_pid(msg.get_cmd());
			suspended_procs.put(msg.get_cmd(), sid);
			System.out.println("*************");
			System.out.println("Suspended Process:"+msg.get_cmd());
			System.out.println("*************");			
		}
		
		else if (msg.get_action().equals("T"))
		{
			msg.set_pid(msg.get_cmd());
			
			if (this.running_procs.containsKey(msg.get_cmd()))
				sid = running_procs.remove(msg.get_cmd());
			else if (this.suspended_procs.containsKey(msg.get_cmd()))
				sid = suspended_procs.remove(msg.get_cmd());
			else if (this.resumed_procs.containsKey(msg.get_cmd()))
				sid = resumed_procs.remove(msg.get_cmd());
			else
			{
				System.out.println("Error: Process does not exist!");
				return;
			}
			System.out.println("*************");
			System.out.println("Terminated Process:"+msg.get_cmd());
			System.out.println("*************");			
		}
		
		else if (msg.get_action().equals("R"))
		{
				
			String cmd = msg.get_cmd();
			String []_tmp = cmd.split(" ");
			String _sid = this.suspended_procs.remove(msg.get_cmd());
			String s_ip_port = this.sid_ipport.get(_sid);
			/* 
			 * if the input cmd is <cmd> <param1> <param2> ...
			 * then we have to split the cmd to get the real cmd and the params.
			 */
			if (_tmp.length > 1)
			{
				cmd = _tmp[0];
				msg.set_cmd(cmd);
				if (this.suspended_procs.remove(cmd)==null)
				{
					System.out.println("pid:"+cmd+" has not been suspended");
					return;
				}
				s_ip_port = _tmp[1];
				sid = ipport_sid.get(s_ip_port);		
			}
			else{ sid = _sid;}
			
			if (s_ip_port==null)
			{
				System.out.println("pid:"+cmd+" has not been suspended");
				return;
			}
			String []tmp = s_ip_port.split(":");
			
			if (tmp.length>0 && tmp.length< 2)
			{
				System.out.println("Error, Please input the valid ip:port");
				return;
			}
			else
			{
				ip = tmp[0];
				port = Integer.valueOf(tmp[1]);					
			}
			msg.set_pid(msg.get_cmd());
		
			System.out.println("****************");
			System.out.println("Resume:"+msg.get_cmd());
			this.resumed_procs.put(msg.get_pid(), sid);
			System.out.println("****************");
		}
		msg.set_slaveid(sid);	
		msg.set_to_ip(ip);
		msg.set_to_port(port);
		
		System.out.format("Dispatching, to ip:%s, port:%d\n", msg.get_toip(), msg.get_toport());
		RESULT res = mp.send2slave(msg);
		if ((res == RESULT.SLAVE_UNREACHABLE) || (res == RESULT.IO_ERROR))
		{
			sids.remove(sid);
			sid_ipport.remove(sid);
		}
	}
	
	public HashMap<String, String> get_pid_ipports_status(String status)
	{
		HashMap<String, String> rpi = new HashMap<String, String>();
		Iterator<Map.Entry<String, String>> rps = null;
		
		if (status.equals("R"))
			rps = this.running_procs.entrySet().iterator();
		else if (status.equals("S"))
			rps = this.suspended_procs.entrySet().iterator();
		
		while(rps.hasNext())
		{
			Map.Entry pid_sid = (Map.Entry)rps.next();
			String pid = (String) pid_sid.getKey();
			String sid = (String) pid_sid.getValue();
			String ip_port = this.sid_ipport.get(sid);
			System.out.println(ip_port);
			rpi.put(pid, ip_port);
		}
	
		return rpi;
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
	
}

