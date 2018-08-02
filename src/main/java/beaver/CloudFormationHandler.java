package beaver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CloudFormationHandler {
    private static final Logger lg = LoggerFactory.getLogger(CloudFormationHandler.class);

    private static CloudFormationHandler cfHander = new CloudFormationHandler();

    private ExecutorService pool = Executors.newCachedThreadPool();

    private Map<String, Future<CloudformationTracker>> trackers = new HashMap<String, Future<CloudformationTracker>>();

    private CloudFormationHandler() {
    }

    public static CloudFormationHandler getHander() {
        return cfHander;
    }

    public void submit(CloudformationTracker tracker) {
        Future<CloudformationTracker> fu = pool.submit(tracker);
        trackers.put(tracker.getStacks().getStackname(), fu);
    }

    public void submitTask(CloudformationTracker cft) {
        Future<CloudformationTracker> tracker = trackers.get(cft.getStacks().getStackname());
        if (tracker == null) {
            this.submit(cft);
            lg.info(String.format("%s -- Submit new tracker task to CloudFormationHandler(%s), %s", cft.getSe(), trackers.size(), cft.getStacks().getStackname()));
            return;
        }

        try {
            if (tracker.isDone()) {
                lg.info(String.format("%s -- Replace history tracker thread: (%s), %s", cft.getSe(), trackers.size(), cft.getStacks().getStackname()));
                Future<CloudformationTracker> fu = pool.submit(cft);
                trackers.put(cft.getStacks().getStackname(), fu);
            } else if (tracker.isCancelled() == false) {
                lg.info(String.format("%s -- Monitoring task still running, continue use old thread.: (%s)", cft.getSe(), trackers.size()));
            }
        } catch (Exception e) {
            lg.error(String.format("%s -- Error submit task! %s", cft.getSe(), e.getMessage()));
        }
    }

}
