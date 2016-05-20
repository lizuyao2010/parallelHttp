import com.fasterxml.jackson.databind.ObjectMapper;
import io.parallec.core.util.PcDateUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

import java.io.IOException;
import java.security.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by lizuyao2010 on 5/19/16.
 */
public class MetricsDaoImpl implements MetricsDao {
    private Node node;
    private Client client;
    public MetricsDaoImpl() {
        node=nodeBuilder().node();
        client=node.client();
    }
    public void storeMetrics(String json, String timestamp, String host)
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Metrics metrics = mapper.readValue(json, Metrics.class);
            Map<String, Object> metricMap = new HashMap<>();
            metricMap.put("cpu", metrics.getCpu());
            metricMap.put("mem", metrics.getMem());
            metricMap.put("TimeStamp", timestamp);
            System.out.println("cpu: " + metrics.getCpu() + " mem: " + metrics.getMem()
                    + " timeStamp: "+metricMap.get("TimeStamp") + " host: " + host);
            client.prepareIndex("metrics", "metric", host).setSource(metricMap).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void close()
    {
        node.close();;
    }
}
