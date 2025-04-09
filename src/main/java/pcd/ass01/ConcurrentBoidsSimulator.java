package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConcurrentBoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private List<Worker> workers;
    private static final int FRAMERATE = 25;
    private int framerate;

    private int nCores = Runtime.getRuntime().availableProcessors() ;

    private Barrier positionBarrier;
    private SynchWorkersView viewBarrier;
    private Barrier initialHandshackePoint;
    private Barrier finalHandshakePoint;

    private int nBoids;

    private boolean isRunning;
    private boolean isStopped;

    public ConcurrentBoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        this.workers = new ArrayList<>();
        initialHandshackePoint = new Barrier(2);
        finalHandshakePoint = new Barrier(2);
//        this.nCores = model.getBoids().size();
    }

    public BoidsModel getModel(){
        return this.model;
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void startSimulation(int nBoids){
        this.nBoids = nBoids;
        try {
            this.initialHandshackePoint.notifyJobDone();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            this.finalHandshakePoint.notifyJobDone();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(){
        while(true){
            try {
                this.initialHandshackePoint.notifyJobDone();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.model.createSimulation(this.nBoids);
            this.isRunning = true;
            this.isStopped = false;
            try {
                this.finalHandshakePoint.notifyJobDone();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.positionBarrier = new Barrier(nCores);
            this.viewBarrier = new SynchWorkersView(nCores);
            this.runSimulation();

        }
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

        while (!isStopped) {
            var t0 = System.currentTimeMillis();

            try{
                this.viewBarrier.waitJobsDone();
            }catch (InterruptedException e ){
                throw new RuntimeException(e);
            }

            if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var framratePeriod = 1000/FRAMERATE;

                if (dtElapsed < framratePeriod) {
                    try {
                        Thread.sleep(framratePeriod - dtElapsed);
                    } catch (Exception ex) {}
                    framerate = FRAMERATE;
                } else {
                    framerate = (int) (1000/dtElapsed);
                }
            }

            try {
                this.viewBarrier.notifyViewUpdated();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

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
            if (view.isPresent()) {
                view.get().updateSuspendResumeButtonText(isRunning ? "Resume" : "Suspend");
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
        if (view.isPresent()) {
            view.get().resetToInitialScreen();
        }

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
