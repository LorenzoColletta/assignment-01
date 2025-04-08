package pcd.ass01;

import java.util.List;

public class Worker extends Thread{
    private List<Boid> boids;
    private BoidsModel model;
    private SynchWorkersView viewBarrier;
    private SynchWorkers positionBarrier;
    private boolean isStopped;

    Worker(List<Boid> boids, BoidsModel model, SynchWorkersView viewBarrier, SynchWorkers positionBarrier){
        this.boids = boids;
        this.model = model;
        this.viewBarrier = viewBarrier;
        this.positionBarrier = positionBarrier;
    }

    @Override
    public void run() {
        super.run();
        while (!this.isStopped){
            for (Boid boid : boids) {
                boid.updateVelocity(model);
            }
            //System.out.println("Velocity Updated");
            try {
                positionBarrier.notifyJobDone();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //System.out.println("Velocity wait terminated");
            for (Boid boid : boids) {
                boid.updatePos(model);
            }
            //System.out.println("Position Updated");
            try {
                viewBarrier.notifyJobDone();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //System.out.println("View Update wait terminated");
        }
    }

    public void setStopped(){
        this.isStopped = true;
    }
}
