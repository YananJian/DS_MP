import common.Constants.RESULT;
import common.MigratableProcess;
import common.Msg;
import common.Constants;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.io.*;

public class MsgProcessor implements Runnable{

	private ProcessManager pm = ProcessManager.getInstance();
	private static MsgProcessor mp = new MsgProcessor();
	private ObjectInputStream input;
	ServerSocket server_sock = null;
	private HashMap<String, Socket> slave_sockets;
	private HashMap<String, ObjectInputStream> slave_inputstm;
	private HashMap<String, ObjectOutputStream> slave_outputstm;
	
	private MsgProcessor()
	{
		slave_sockets = new HashMap<String, Socket>();
		slave_inputstm = new HashMap<String, ObjectInputStream>();
		slave_outputstm = new HashMap<String, ObjectOutputStream>();
	}
	
	public static synchronized MsgProcessor getInstance()
	{
		return mp;
	}
	
	public RESULT send2slave(Msg msg){
		try {
			if (!msg.get_sid().equals(null)){
				Socket socket = this.slave_sockets.get(msg.get_sid());
				ObjectOutputStream outputstm = this.slave_outputstm.get(msg.get_sid());
				if (socket == null)
				{
					System.out.println("Slave not exists");
					return RESULT.SID_NOT_MACHING_SLAVE;
				}
				if (outputstm == null)
				{
					System.out.println("Output Stream null");
					outputstm = new ObjectOutputStream(socket.getOutputStream());
					this.slave_outputstm.put(msg.get_sid(), outputstm);
				}
				outputstm.writeObject(msg);
				outputstm.flush();
				return RESULT.SUCCESS;
			}
			else
			{
				System.out.println("SID null");
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.slave_sockets.remove(msg.get_sid());
			this.slave_inputstm.remove(msg.get_sid());
			this.slave_outputstm.remove(msg.get_sid());
			return RESULT.SLAVE_UNREACHABLE;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return RESULT.IO_ERROR;
		} 
		return RESULT.SID_NO_EXISTS;
	}
	
	public void read_slave(String sid, Socket sock)
	{
		//this.input
		
	}
	
	public void parseMsg(Msg m)
	{
		ProcessManager pm = ProcessManager.getInstance();
		String sid = m.get_sid();
		String pid = m.get_pid();
		if (m.get_status() == Constants.Status.IDLE)
		{
			pm.sids.add(sid);			
			//pm.set_sid_status(sid, Constants.Status.IDLE);
		}
		else if (m.get_status() == Constants.Status.SUSPENDED)
		{
			System.out.println("In MsgProcessor, pid:"+m.get_pid());
			pm.suspended_procs.put(m.get_pid(), pm.sid_ipport.get(m.get_sid()));
		}
		
		else{ System.out.println("Slave "+m.get_status()+" sid:"+sid);}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ProcessManager pm = ProcessManager.getInstance();
		try {
			server_sock = new ServerSocket(Constants.PORT_MASTER);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (server_sock == null){
			System.out.format("can not setup server socket on port:%d", Constants.PORT_MASTER);
			return;
		}

		while (true)
		{
			Socket cli_sock;
			try {
				System.out.println("Preparing to accept");
				cli_sock = server_sock.accept();
				System.out.println("New Accepted Conn");
				String cli_ip = cli_sock.getInetAddress().getHostAddress();
				int cli_port = cli_sock.getPort();
				
				System.out.format("Connected cli ip:%s\t cli port:%d\n", cli_ip, cli_port);
				String sid = pm.gen_slaveid(cli_ip, cli_port);
				this.slave_sockets.put(sid, cli_sock);
				pm.ipport_sid.put(cli_ip+":"+String.valueOf(cli_port), sid);
				pm.sid_ipport.put(sid, cli_ip+":"+String.valueOf(cli_port));
				ObjectInputStream inputstm = this.slave_inputstm.get(sid);
				ObjectOutputStream outputstm = this.slave_outputstm.get(sid);
				if (inputstm == null)
				{
					inputstm = new ObjectInputStream(cli_sock.getInputStream());
					this.slave_inputstm.put(sid, inputstm);
				}
				if (outputstm == null)
				{
					outputstm = new ObjectOutputStream(cli_sock.getOutputStream());
					this.slave_outputstm.put(sid, outputstm);			
				}
				String ip_port = pm.get_sidmap(sid);
				if (ip_port == null)
					pm.add_sidmap(sid, cli_ip+":"+String.valueOf(cli_port));
			
				Object o = inputstm.readObject();				
				if (!(o instanceof Msg))
				{
					System.out.println("Slave is not sending a Msg");
					continue;
				}
				Msg m = (Msg) o;
				if (m.get_sid().equals(""))
					m.set_slaveid(sid);
				System.out.println("PID:"+m.get_pid());
				System.out.println("parsing Msg");
				this.parseMsg(m);
								
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
}
