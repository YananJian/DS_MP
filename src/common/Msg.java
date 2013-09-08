package common;

public class Msg {

	private int proc_id;
	private String act;
	private String cmd;
	private String slave_id;
	private String to_ip;
	private int to_port;
	
	public Msg(String act, String cmd)
	{
		this.act = act;
		this.cmd = cmd;
	}
	
	public void set_sid(String sid){
		
		this.slave_id = sid;
		
	}
	
	public String get_sid(){
		
		return this.slave_id;
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
