package engine;

public class simpleThread {
    private Runnable runnable;
    private Thread thread;
    private boolean stop;
    simpleThread (){
        thread = new Thread(this::process);
        stop = false;
    }
    private void process(){
        while (!stop) {
            //synchronized (this) {
                runnable.run();
            //}
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                System.out.println("exception in engine.simpleThread cycle");
            }
        }
    }

    public void changeProcess(Runnable runnable){
        //synchronized (this){
            this.runnable = runnable;
        //}
    }

    public void start(){
        thread.start();
    }
    public void stop(){
        this.stop = true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.out.println("exception in engine.simpleThread");
        }
    }
}
