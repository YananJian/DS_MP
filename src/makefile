#TINY_ABSTRACT_TRAIN = /afs/cs.cmu.edu/project/bigML/dbpedia/abstract.tiny.train

.SUFFIXES: .java .class
.java.class:
	javac $<

CLASSES = NBTrain.class NBTest.class Agg.class NBTestCM.class

all: $(CLASSES)

ProcessManager: ProcessManager.class

CmdProcessor: CmdProcessor.class

MsgProcessor: MsgProcessor.class

Slave: Slave.class 

GrepProcess: GrepProcess.class

CpFile: CpFile.class

CountLines: CountLines.class

pm: ProcessManager CmdProcessor MsgProcessor Slave GrepProcess CpFile CountLines
	java ProcessManager

clean:
	rm $(CLASSES)
