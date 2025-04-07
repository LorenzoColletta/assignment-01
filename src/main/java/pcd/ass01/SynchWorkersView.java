package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchWorkersView {
    private final int nWorkers;
    private int nJobsDone;
    private final Lock mutex;
    private final Condition updatePosition, updateView;

    SynchWorkersView(int nWorkers){
        this.nWorkers = nWorkers;
        this.nJobsDone = 0;
        mutex = new ReentrantLock();
        updatePosition = mutex.newCondition();
        updateView = mutex.newCondition();
    }

    public Void notifyJobDone() throws InterruptedException{
        try{
            mutex.lock();
            nJobsDone++;
            while (nJobsDone != nWorkers && nJobsDone != 0) {
                updatePosition.await();
            }
            if(nJobsDone == nWorkers)
                updateView.signal();
            while (nJobsDone == nWorkers) {
                updatePosition.await();
            }

        }finally {
            mutex.unlock();
        }
        return null;
    }

    public Void waitJobsDone() throws InterruptedException{
        try{
            mutex.lock();
            while(nJobsDone != nWorkers){
                updateView.await();
            }
        }finally {
            mutex.unlock();
        }
        return null;
    }

    public Void notifyViewUpdated() throws InterruptedException{
        try{
            mutex.lock();
            this.nJobsDone = 0;
            updatePosition.signalAll();
        }finally {
            mutex.unlock();
        }
        return null;
    }

}
