package common;

public class Constants {

	public static final int PORT_MASTER = 10000;
	public static final String IP_MASTER = "0.0.0.0";
	public static final String MODE = "DEBUG";
	public enum Status {IDLE, SUSPENDED, TERMINATED, BUSY};
	public enum RESULT {SUCCESS, SID_NOT_MACHING_SLAVE, 
						SLAVE_UNREACHABLE, IO_ERROR, 
						UNKNOWN_ERROR, SID_NO_EXISTS, 
						PROC_NOT_LAUNCHED};
}
