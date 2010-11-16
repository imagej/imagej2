package ijx.exec;

import java.util.concurrent.*;

public class FutureTaskTest {

    public FutureTaskTest() {
    }

    public static void main(String[] args) {

        CommandDispatcher sd = new CommandDispatcher();
        FutureTask task = sd.doCommand("somethin");
        System.out.println("Do some other stuff...");
        try { // wait for completion and get returned object
            System.out.println(
                    task.get(2000, TimeUnit.MILLISECONDS));
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (TimeoutException ex) {
            System.out.println("Timed out!");
            //ex.printStackTrace();
        }
        sd.shutDown();

    }
}

class CommandDispatcher {

    ExecutorService executor;

    public CommandDispatcher() {
        executor = Executors.newSingleThreadExecutor();
    }

    public FutureTask doCommand(String cmd) {
        FutureTask task = new FutureTask(new DoCommand(cmd));
        executor.submit(task);
        return task;
    }

    public void shutDown() {
        executor.shutdown();
    }

    class DoCommand implements Callable {

        String cmd = "";

        public DoCommand(String cmd) {
            this.cmd = cmd;
        }

        public String call() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return "DoCommand on command: " + cmd;
        }
    }
}
