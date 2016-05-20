import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.parallec.core.*;
import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcStringUtils;
import org.apache.http.util.Asserts;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by lizuyao2010 on 5/18/16.
 */
public class App {
    protected static final Logger logger = LoggerFactory
            .getLogger(App.class);
    public static void main(String[] args)
    {
        query(Arrays.asList("localhost","192.168.1.110","www.parallec.io"),"/metric");
    }
    public static void query(List<String> nodes, String method)
    {
        MetricsDao metricsDao = new MetricsDaoImpl();
        ParallelClient pc = new ParallelClient();
        ParallelTask task = pc
                .prepareHttpGet(method)
                .async()
                .setHttpPort(4567)
                .setConcurrency(10000)
                .setTargetHostsFromList(nodes)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                                            Map<String, Object> responseContext) {
                        metricsDao.storeMetrics(res.getResponseContent(),PcDateUtils.getNowDateTimeStrStandard(), res.getHost());
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
        metricsDao.close();
        pc.releaseExternalResources();
    }
    private static void storeData(ResponseOnSingleTask res, Map<String, Object> responseContext) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Metrics metrics = mapper.readValue(res.getResponseContent(), Metrics.class);
            Map<String, Object> metricMap = new HashMap<>();
            metricMap.put("cpu", metrics.getCpu());
            metricMap.put("mem", metrics.getMem());
            metricMap.put("TimeStamp", PcDateUtils.getNowDateTimeStrStandard());
            logger.info("cpu:" + metrics.getCpu() + " mem: " + metrics.getMem()
                    + " host: " + res.getHost() + " timeStamp: "+metricMap.get("TimeStamp"));
            Client client = (Client) responseContext.get("Client");
            client.prepareIndex("metrics", "metric", res.getHost()).setSource(metricMap).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
