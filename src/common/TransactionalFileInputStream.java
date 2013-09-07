package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable{
	
	String fname;
	int fd;
	public TransactionalFileInputStream(String _fileName) {
		this.fname = _fileName;
		this.fd = 0;
	}
	@Override
	public int read() throws IOException{
		
		return 0;
		
	}

}
