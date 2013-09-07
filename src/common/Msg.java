package common;

public class Msg {

	private int proc_id;
	private char act;
	private String cmd;
	private int slave_id;
	
	Msg(int sid, char act, String cmd)
	{
		this.slave_id = sid;
		this.act = act;
		this.cmd = cmd;
	}
	
	public int get_sid(){
		
		return this.slave_id;
	}
	
	public char get_action(){
		
		return this.act;
	}
	
	public String get_cmd(){
		
		return this.cmd;
	} 
}
