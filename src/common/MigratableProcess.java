package common;

public interface MigratableProcess extends java.lang.Runnable, java.io.Serializable {

    void run();
    
    void suspend();
    
    void terminate();

}