package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConcurrentBoidsSimulator {

    private static final int FRAMERATE = 25;

    private BoidsModel model;
    private List<Worker> workers;

    private Barrier positionBarrier;
    private SynchWorkersView viewBarrier;
    private Barrier initialHandshackePoint;
    private Barrier finalHandshakePoint;

    private int nCores;
    private int nBoids;

    private int nFrames;
    private boolean isRunning;
    private boolean isStopped;

    public ConcurrentBoidsSimulator(BoidsModel model) {
        this.model = model;
        this.workers = new ArrayList<>();
        initialHandshackePoint = new Barrier(2);
        finalHandshakePoint = new Barrier(2);
    }

    public BoidsModel getModel(){
        return this.model;
    }


    public void startSimulation(int nBoids){

    }

    public void startSimulation(int nBoids, int nWorkers, int nFrames){
        this.nBoids = nBoids;
        this.nCores = nWorkers;
        this.nFrames = nFrames;
//        try {
//            this.initialHandshackePoint.notifyJobDone();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            this.finalHandshakePoint.notifyJobDone();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void run(){
//        try {
//            this.initialHandshackePoint.notifyJobDone();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        this.model.createSimulation(this.nBoids);
        this.isRunning = true;
        this.isStopped = false;
//        try {
//            this.finalHandshakePoint.notifyJobDone();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        this.positionBarrier = new Barrier(nCores);
        this.viewBarrier = new SynchWorkersView(nCores);
        this.runSimulation();

    }

    public void runSimulation() {
        int nBoids = model.getBoids().size();

        List<List<Boid>> partitions = IntStream.range(0, nCores)
                .mapToObj(i -> model.getBoids().subList(i * nBoids / nCores, (i + 1) * nBoids / nCores)).collect(Collectors.toList());

        for(int i = 0; i < nCores; i++){
//            Thread.ofVirtual().start(new Worker(partitions.get(i), model, viewBarrier, positionBarrier));
            this.workers.add(new Worker(partitions.get(i), model, viewBarrier, positionBarrier));
        }

        workers.forEach(Thread::start);

        for (int i = 0; i < nFrames - 1; i++) {

            try{
                this.viewBarrier.waitJobsDone();
            }catch (InterruptedException e ){
                throw new RuntimeException(e);
            }

            try {
                this.viewBarrier.notifyViewUpdated();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try{
            this.viewBarrier.waitJobsDone();
        }catch (InterruptedException e ){
            throw new RuntimeException(e);
        }
        workers.forEach(Worker::setStopped);
        workers.clear();
        try {
            this.viewBarrier.notifyViewUpdated();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void toggleSuspendResume() {
        try {
            if(this.isRunning) {
                this.viewBarrier.notifySuspension();
                this.isRunning = false;
            }else {
                this.viewBarrier.notifyResume();
                this.isRunning = true;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopSimulation() {
        try {
            this.viewBarrier.waitViewUpdate();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.isStopped = true;
        workers.forEach(Worker::setStopped);
        workers.clear();

        this.viewBarrier.notifyStop();

    }

    public void setSeparationWeight(double value) {
        this.model.setSeparationWeight(value);
    }

    public void setAlignmentWeight(double value) {
        this.model.setAlignmentWeight(value);
    }

    public void setCohesionWeight(double value) {
        this.model.setCohesionWeight(value);
    }

}
