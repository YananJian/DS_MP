package test;

import common.MigratableProcess;
import common.GrepProcess;

public class TestProcess {

	public static void main(String args[]) throws Exception
	{
		String _args[] = {"grep", "in.t", "out.t"};
		MigratableProcess p = new GrepProcess(_args);
		p.run();
		p.suspend();
		p.terminate();
	}
	
}
