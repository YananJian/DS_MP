import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import common.Msg;

public class CmdProcessor implements Runnable{
	
	private static CmdProcessor cp = new CmdProcessor();

	private CmdProcessor(){};
	public static synchronized CmdProcessor getInstance()
	{
		return cp;
	}
	
	public void print_status(HashMap<String, String> p_stats)
	{
		if (p_stats.isEmpty())
		{
			System.out.println("No Processes");
			return;
		}
		Iterator<Map.Entry<String, String>> rps = p_stats.entrySet().iterator();
			
		while(rps.hasNext())
		{
			Map.Entry pid_ipport = (Map.Entry)rps.next();
			String pid = (String) pid_ipport.getKey();
			String ipport = (String) pid_ipport.getValue();			
			System.out.format("pid:%s, ip:port:%s\n", pid, ipport);
		}
		
	}
	
	@Override
	public void run() {
		System.out.println("Input cmd here, format: action cmd");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		// TODO Auto-generated method stub
		while(true){
			try {
				ProcessManager pm = ProcessManager.getInstance();				
				HashMap<String, String> run_p = pm.get_pid_ipports_status("R");
				HashMap<String, String> sus_p = pm.get_pid_ipports_status("S");
				System.out.println("*****Running Processes:******");
				print_status(run_p);
				System.out.println("******************************");
				System.out.println("*****Suspended Processes:******");
				print_status(sus_p);
				System.out.println("******************************");
				System.out.println("Input cmd here, format: <action> <cmd>");
				String cmd = br.readLine();
				// input cmd format: act cmd
				String []cmds = cmd.split(" ");
				if (cmds.length < 2)
				{
					System.out.println("Illegal Cmd. Format: action cmd");
					continue;
				}
						
				String tmp = cmd.substring(cmd.indexOf(" ")).trim();
				Msg msg = new Msg(cmds[0], tmp,"");
				pm.dispatchMsg(msg);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	

}
