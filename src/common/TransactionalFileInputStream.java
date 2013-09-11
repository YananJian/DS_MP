package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String fname;
	int fd;
	public TransactionalFileInputStream(String _fileName) {
		this.fname = _fileName;
		this.fd = 0;
	}
	
	@Override
	public int read() throws IOException{
		@SuppressWarnings("resource")
		FileInputStream fs = new FileInputStream(this.fname);
		fs.skip(fd);
		int ret = fs.read();
		if (ret != -1)
			fd += 1;
		return ret;
	}
	
	@Override
	public int read(byte[] b) throws IOException  {
		FileInputStream fs = new FileInputStream(this.fname);
		fs.skip(fd);
		int ret = fs.read(b);
		fs.close();

		if (ret != -1)
			fd += ret;

		return ret;
	}

	/*Reads len bytes into b starting at b[off]
	*/
	@Override
	public int read(byte[] b, int off, int len) throws IOException  {
		FileInputStream fs = new FileInputStream(this.fname);
		fs.skip(fd);
		int ret = fs.read(b, off, len);
		fs.close();

		if (ret != -1)
			fd += ret;

		return ret;
	}

}
