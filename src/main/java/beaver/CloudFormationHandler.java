package beaver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CloudFormationHandler {
    private static CloudFormationHandler cfHander = new CloudFormationHandler();

    private ExecutorService pool = Executors.newCachedThreadPool();

    private List<Future<CloudformationTracker>> sthandlers = new ArrayList<>();

    private CloudFormationHandler() {
    }

    public static CloudFormationHandler getHander() {
        return cfHander;
    }

    public void submit(CloudformationTracker tracker) {
        Future<CloudformationTracker> fu = pool.submit(tracker);
        sthandlers.add(fu);
    }

    public List<Future<CloudformationTracker>> getAllStacks() {
        return sthandlers;
    }

}
