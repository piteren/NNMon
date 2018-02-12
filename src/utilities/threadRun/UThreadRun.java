package utilities.threadRun;

/**
 * runs as thread method runTR(..) from given UTRobject
 */
public class UThreadRun implements Runnable {
    
    private final Thread        myThread;           //thread
    private final UTRobject     myTRobject;         //object that implements UTR interface
    private final int           myTParam;           //thread run paramater
    
    //constructor(running object)
    public UThreadRun(UTRobject myTRob){
        myTRobject = myTRob;
        myThread = new Thread(this);
        myTParam = 0;
        myThread.start();
    }
    
    //constructor(running object, int param)
    public UThreadRun(UTRobject myTRob, int mIP){
        myTRobject = myTRob;
        myThread = new Thread(this);
        myTParam = mIP;
        myThread.start();
    }
    
    public UTRobject getMyTRobject(){
        return myTRobject;
    }
    
    public void interrupt(){
        myThread.interrupt();
    }
        
    public void join() throws InterruptedException{
        myThread.join();
    }
    
    public boolean isTerminated(){
        return myThread.getState()==Thread.State.TERMINATED;
    }
    
    @Override
    public void run() {
        myTRobject.runTR(myTParam);
    }
}