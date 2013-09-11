package common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String fname;
	
	public TransactionalFileOutputStream(String _fileName) {
		this.fname = _fileName;
	}
	
	@Override
	public void write(int b) throws IOException{
		
		@SuppressWarnings("resource")
		FileOutputStream fos = new FileOutputStream(this.fname, true);
		fos.write(b);
	}
	
	@Override
	public void write(byte b[]) throws IOException
	{
		@SuppressWarnings("resource")
		FileOutputStream fos = new FileOutputStream(this.fname, true);
		fos.write(b);	
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		@SuppressWarnings("resource")
		FileOutputStream fos = new FileOutputStream(this.fname, true);
		fos.write(b, off, len);	
	}
	
}
