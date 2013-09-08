import java.io.IOException;
import java.io.*;
import common.Msg;

public class CmdProcessor implements Runnable{

	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		// TODO Auto-generated method stub
		while(true){
			try {
				String cmd = br.readLine();
				// input cmd format: act cmd
				String []cmds = cmd.split(" ");
				if (cmds.length != 2)
				{
					System.out.println("Illegal Cmd. Format: action cmd");
					continue;
				}
				Msg msg = new Msg(cmds[0], cmds[1]);
				ProcessManager pm = ProcessManager.getInstance();
				pm.msgQueue.add(msg);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
