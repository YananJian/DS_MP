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
	
	public void send2slave(Msg msg){
		try {
			if (!msg.get_sid().equals(null)){
				Socket socket = this.slave_sockets.get(msg.get_sid());
				ObjectOutputStream outputstm = this.slave_outputstm.get(msg.get_sid());
				if (socket.equals(null))
				{
					socket = new Socket(msg.get_toip(), msg.get_toport());
					this.slave_sockets.put(msg.get_sid(), socket);
				}
				if (outputstm.equals(null))
				{
					outputstm = new ObjectOutputStream(socket.getOutputStream());
					this.slave_outputstm.put(msg.get_sid(), outputstm);
				}
				outputstm.writeObject(msg);
				System.out.println("Slave received msg");
				outputstm.flush();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void read_slave(String sid, Socket sock)
	{
		//this.input
		
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
				String cli_ip = cli_sock.getInetAddress().getHostAddress();
				int cli_port = cli_sock.getPort();
				
				System.out.format("cli ip:%s\t cli port:%d\n", cli_ip, cli_port);
				String sid = pm.gen_slaveid(cli_ip, cli_port);
				ObjectInputStream inputstm = this.slave_inputstm.get(sid);
				//ObjectInputStream inputstm = new ObjectInputStream(cli_sock.getInputStream());
				//System.out.println(inputstm == null);
				if (inputstm == null)
				{
					System.out.println(cli_sock.getInputStream());
					inputstm = new ObjectInputStream(cli_sock.getInputStream());
					this.slave_inputstm.put(sid, inputstm);
				}
				String ip_port = pm.get_sidmap(sid);
				if (ip_port == null)
					pm.add_sidmap(sid, cli_ip+":"+String.valueOf(cli_port));
			
				Object o = inputstm.readObject();
				//Object o = cli_sock.getInputStream().read();
				if (!(o instanceof Msg))
				{
					System.out.println("Slave is not sending a Msg");
					continue;
				}
				Msg m = (Msg) o;
				if (m.get_status() == Constants.Status.IDLE)
				{
					pm.ideal_sids.add(sid);
					pm.set_sid_status(sid, Constants.Status.IDLE);
				}
			
				
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
