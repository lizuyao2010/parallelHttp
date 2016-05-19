/**
 * Created by lizuyao2010 on 5/19/16.
 */
import io.parallec.core.HostsSourceType;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.util.PcStringUtils;

import java.util.Map;
public class HttpBasicAsyncRunProgressPollingApp {

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        ParallelClient pc = new ParallelClient();

        ParallelTask task = pc.prepareHttpGet("").async()
                .setConcurrency(500)
                .setTargetHostsFromLineByLineText("http://www.parallec.io/userdata/sample_target_hosts_top100_old.txt",
                        HostsSourceType.URL)
                .execute(new ParallecResponseHandler() {
                    public void onCompleted(ResponseOnSingleTask res,
                                            Map<String, Object> responseContext) {
                        System.out.println("Responose Code:"
                                + res.getStatusCode() + " host: "
                                + res.getHost());
                    }
                });

        while (!task.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        task.getProgress(), task.getTaskId()));
                pc.logHealth();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out
                .println("Result Summary\n "
                        + PcStringUtils.renderJson(task
                        .getAggregateResultFullSummary()));

        System.out
                .println("Result Brief Summary\n "
                        + PcStringUtils.renderJson(task
                        .getAggregateResultCountSummary()));
        pc.releaseExternalResources();
    }
}