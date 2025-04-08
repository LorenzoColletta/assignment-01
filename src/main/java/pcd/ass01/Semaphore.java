package pcd.ass01;

import java.util.concurrent.locks.Condition;

public class Semaphore {
    private int counter;

    Semaphore(int initialValue){
        this.counter = initialValue;
    }

    public synchronized void acquire() throws InterruptedException{
        if(counter == 0){
            wait();
        } else {
            this.counter--;
        }
    }

    public synchronized void release() throws InterruptedException {
        if(this.counter == 0){
            notify();
        } else {
            this.counter++;
        }
    }
}
