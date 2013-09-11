import java.io.IOException;
import java.io.*;
import common.Msg;

public class CmdProcessor implements Runnable{
	
	private static CmdProcessor cp = new CmdProcessor();

	private CmdProcessor(){};
	public static synchronized CmdProcessor getInstance()
	{
		return cp;
	}
	
	@Override
	public void run() {
		System.out.println("Input cmd here, format: action cmd");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		// TODO Auto-generated method stub
		while(true){
			try {
				System.out.println("Input cmd here, format: action cmd");
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
				ProcessManager pm = ProcessManager.getInstance();
				pm.dispatchMsg(msg);
				//pm.msgQueue.add(msg);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	

}
