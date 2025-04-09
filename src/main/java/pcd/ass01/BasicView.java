package pcd.ass01;

public class BasicView extends Thread implements View{

    private static final int N_BOIDS_SIM_1 = 3;
    private static final int N_BOIDS_SIM_2 = 3;
    private static final long SLEEP_TIME = 3;
    private static final double PARAMETERS_VALUE = 0.5;

    private final ConcurrentBoidsSimulator simulator;

    BasicView(ConcurrentBoidsSimulator simulator){
        this.simulator = simulator;
        this.start();
    }

    @Override
    public void run(){
        // first simulation start
        simulator.startSimulation(N_BOIDS_SIM_1);
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // suspend simulation
        simulator.toggleSuspendResume();

        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // resume simulation
        simulator.toggleSuspendResume();

        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        simulator.stopSimulation();

        // second simulation start
        simulator.startSimulation(N_BOIDS_SIM_1);

        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // change cohesion, alignment and separation parameters
        simulator.setAlignmentWeight(PARAMETERS_VALUE);
        simulator.setCohesionWeight(PARAMETERS_VALUE);
        simulator.setSeparationWeight(PARAMETERS_VALUE);

        System.exit(0);
    }

    @Override
    public void update(int frameRate) {
        System.out.println("FrameRate: " + frameRate);

    }

    @Override
    public void resetToInitialScreen() {

    }
}
