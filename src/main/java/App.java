import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.parallec.core.FilterRegex;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.util.PcDateUtils;
import org.apache.http.util.Asserts;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
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
        query(Arrays.asList("localhost"),"/metric");
    }
    public static void query(List<String> nodes, String method)
    {
        MetricsDao metricsDao = new MetricsDaoImpl();
        ParallelClient pc = new ParallelClient();
        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc
                .prepareHttpGet(method)
                .setHttpPort(4567)
                .setConcurrency(10000)
                .handleInWorker()
                .setTargetHostsFromList(nodes)
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                                            Map<String, Object> responseContext) {
                        metricsDao.storeMetrics(res.getResponseContent(),PcDateUtils.getNowDateTimeStrStandard(), res.getHost());
                    }
                });
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
