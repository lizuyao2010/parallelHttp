/**
 * Created by lizuyao2010 on 5/19/16.
 */
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import io.parallec.core.HostsSourceType;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.util.PcDateUtils;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.Client;
public class Http100WebAggregateToElasticSearchApp {

    public static void main(String[] args) {
        ParallelClient pc = new ParallelClient();
        org.elasticsearch.node.Node node = nodeBuilder().node(); //elastic client initialize
        HashMap<String, Object> responseContext = new HashMap<String, Object>();
        responseContext.put("Client", node.client());
        pc.prepareHttpGet("")
                .setConcurrency(1000)
                .setTargetHostsFromLineByLineText("http://www.parallec.io/userdata/sample_target_hosts_top100_old.txt",
                        HostsSourceType.URL)
                .setResponseContext(responseContext)
                .execute( new ParallecResponseHandler() {
                    public void onCompleted(ResponseOnSingleTask res,
                                            Map<String, Object> responseContext) {
                        Map<String, Object> metricMap = new HashMap<String, Object>();
                        metricMap.put("StatusCode", res.getStatusCode().replaceAll(" ", "_"));
                        metricMap.put("LastUpdated",PcDateUtils.getNowDateTimeStrStandard());
                        metricMap.put("NodeGroupType", "Web100");
                        Client client = (Client) responseContext.get("Client");
                        client.prepareIndex("local", "parallec", res.getHost()).setSource(metricMap).execute();
                    }
                });
        node.close(); pc.releaseExternalResources();
    }


}