package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchWorkers {

    private final int nWorkers;
    private int nJobsDone;
    private final Lock mutex;
    private final Condition cond;

    SynchWorkers(int nWorkers){
        this.nWorkers = nWorkers;
        this.nJobsDone = 0;
        mutex = new ReentrantLock();
        cond = mutex.newCondition();
    }

    public Void notifyJobDone() throws InterruptedException{
        try{
            mutex.lock();
            nJobsDone++;
            while (nJobsDone != 0  && nJobsDone != nWorkers) {
                cond.await();
            }
            if(nJobsDone == nWorkers){
                nJobsDone = 0;
                cond.signalAll();
            }
        }finally {
            mutex.unlock();
        }
        return null;
    }

}
