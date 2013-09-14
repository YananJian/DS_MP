import java.io.BufferedInputStream;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.sql.Time;

import common.*;


public class CpFile implements MigratableProcess{

	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	
	private volatile boolean suspending;
	public CpFile(String args[]) throws Exception
	{
		if (args.length < 2)
		{
		    System.out.println("usage: CpFile <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");	
		}
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1]);
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub

		 PrintStream out = new PrintStream(outFile);
		 BufferedReader in = new BufferedReader(new InputStreamReader(inFile));
		 try {
				while (!suspending) {
					String line = in.readLine();

					if (line == null) break;
					
					out.println(line);
					
					// Make cp take longer so that we don't require extremely large files for interesting results
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// ignore it
					}
				}
			} catch (EOFException e) {
				//End of File
			} catch (IOException e) {
				System.out.println ("GrepProcess: Error: " + e);
			}


			suspending = false;
		
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		suspending = true;
		//this.suspend();
		/*while (suspending){ try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}*/
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		suspending = false;
	}

	@Override
	public void print() {
		// TODO Auto-generated method stub
		System.out.println("This is CpFile Process!");
	}

}
