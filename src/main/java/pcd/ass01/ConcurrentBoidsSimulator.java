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
    private static final int FRAMERATE = 120;
    private int framerate;
    private int nCores = Runtime.getRuntime().availableProcessors() ;
//    private int nCores;
    private SynchWorkers positionBarrier;
    private SynchWorkersView viewBarrier;

    public ConcurrentBoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        this.workers = new ArrayList<>();
//        this.nCores = model.getBoids().size();
        this.positionBarrier = new SynchWorkers(nCores);
        this.viewBarrier = new SynchWorkersView(nCores);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
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

        while (true) {
            var t0 = System.currentTimeMillis();
            var boids = model.getBoids();

            try{
                this.viewBarrier.waitJobsDone();
            }catch (Exception e ){}

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

            try{
                this.viewBarrier.notifyViewUpdated();
            }catch (Exception e ){}

        }
    }
}
