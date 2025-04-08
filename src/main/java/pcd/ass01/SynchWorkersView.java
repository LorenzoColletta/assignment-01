package pcd.ass01;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchWorkersView {
    private final int nWorkers;
    private int nJobsDone;
    private final Lock mutex;
    private final Condition updatePosition, updateView, updateState;
    private boolean isRunning;
    private boolean onStop;

    SynchWorkersView(int nWorkers){
        this.nWorkers = nWorkers;
        this.nJobsDone = 0;
        mutex = new ReentrantLock();
        updatePosition = mutex.newCondition();
        updateView = mutex.newCondition();
        updateState = mutex.newCondition();
        this.isRunning = true;
        this.onStop = false;
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
            while(nJobsDone != nWorkers || !this.isRunning){
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
            if(this.onStop){
                updateState.signal();
                while (this.onStop){
                    updateView.await();
                }
            } else {
                updatePosition.signalAll();
            }
        } finally {
            mutex.unlock();
        }
        return null;
    }

    public Void notifySuspension() throws  InterruptedException{
        try {
            mutex.lock();
            this.isRunning = false;
        } finally {
            mutex.unlock();
        }
        return null;
    }

    public Void notifyResume() throws InterruptedException {
        try {
            mutex.lock();
            this.isRunning = true;
            updateView.signal();
        } finally {
            mutex.unlock();
        }
        return null;
    }

    public void waitViewUpdate() throws  InterruptedException {
        try {
            mutex.lock();
            this.onStop = true;
            while(nJobsDone != 0 && this.isRunning){
                updateState.await();
            }
        } finally {
            mutex.unlock();
        }
    }

    public void notifyStop(){
        try {
            mutex.lock();
            this.onStop = false;
            if(!this.isRunning)
                this.isRunning = true;
            updateView.signal();
        } finally {
            mutex.unlock();
        }
    }
}
