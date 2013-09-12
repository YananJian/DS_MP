package common;

public class Msg implements java.io.Serializable{

	private String act = "";
	private String cmd = "";
	private String slave_id = "";
	private String to_ip = "";
	private int to_port = 0;
	private Constants.Status status;
	private String pid = "";

	private Object o = null;
	
	public Msg(String act, String cmd, String pid)
	{
		this.act = act;
		this.cmd = cmd;
		this.pid = pid;
	}        
	
	public void set_slaveid(String slaveid){
		this.slave_id = slaveid;
		
	}
	
	public void set_cmd(String cmd)
	{
		this.cmd = cmd;
	}
	
	public void set_to_ip(String ip)
	{
		this.to_ip = ip;
	}
	
	public void set_to_port(int port)
	{
		this.to_port = port;
	}
	
	public void set_pid(String pid)
	{
		this.pid = pid;
	}
	
	public void set_obj(Object o)
	{
		this.o = o;
	}
	
	public void set_status(Constants.Status s){
		this.status = s;		
	}
	
	public Constants.Status get_status(){
		return this.status;		
	}
	
	public String get_sid(){
		
		return this.slave_id;
	}
	
	public String get_pid()
	{
		return this.pid;
	}
	
	public Object get_obj()
	{
		return this.o;
	}
	
	public String get_action(){
		
		return this.act;
	}
	
	public String get_cmd(){
		
		return this.cmd;
	} 
	
	public String get_toip(){
		return this.to_ip;
	}
	
	public int get_toport(){
		
		return this.to_port;
	}
	
}
