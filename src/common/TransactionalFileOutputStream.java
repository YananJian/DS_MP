package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{

	String fname;
	int fd;
	public TransactionalFileOutputStream(String _fileName) {
		this.fname = _fileName;
		this.fd = 0;
	}
	@Override
	public void write(int c) throws IOException{
		
		
	}

	
}
