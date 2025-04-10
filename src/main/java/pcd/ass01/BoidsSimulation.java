package pcd.ass01;

public class BoidsSimulation {
	private	static final int N_CORES = 4;

	private static final int N_BOIDS_SIM_1 = 1500;
	private static final int N_FRAMES = 1500;
	private static final int N_WORKERS = 1;


	final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1000; 
	final static int ENVIRONMENT_HEIGHT = 1000;
    static final double MAX_SPEED = 4.0;
    static final double PERCEPTION_RADIUS = 50.0;
    static final double AVOID_RADIUS = 20.0;

	final static int SCREEN_WIDTH = 800; 
	final static int SCREEN_HEIGHT = 800; 


    public static void main(String[] args) {

    	var model = new BoidsModel(
    					N_BOIDS_SIM_1,
    					SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT, 
    					ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
    					MAX_SPEED,
    					PERCEPTION_RADIUS,
    					AVOID_RADIUS); 
    	var sim = new ConcurrentBoidsSimulator(model);
//    	var view = new BoidsView(sim, SCREEN_WIDTH, SCREEN_HEIGHT);
//    	var view = new BasicView(sim);
//    	sim.attachView(view);

		for(int i = 1; i < N_CORES + 1; i++){
			var t0 = System.currentTimeMillis();
			sim.startSimulation(N_BOIDS_SIM_1, i, N_FRAMES);
			sim.run();
			var t1 = System.currentTimeMillis();
			System.out.println("N_BOIDS: " + N_BOIDS_SIM_1 + " N_FRAMES: " + N_FRAMES + " N_core: " + i + " Elapsed time: " + (t1 - t0));
		}

//		for(int i = 1; i < N_CORES + 1; i++){
//			var t0 = System.currentTimeMillis();
//			int nBoids = (int)(Math.cbrt(i) * N_BOIDS_SIM_1);
//			sim.startSimulation(nBoids, i, N_FRAMES);
//			sim.run();
//			var t1 = System.currentTimeMillis();
//			System.out.println("N_BOIDS: " + nBoids + " N_FRAMES: " + N_FRAMES + " N_core: " + i + " Elapsed time: " + (t1 - t0));
//		}


    }
}
