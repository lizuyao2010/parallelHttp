/**
 * Created by lizuyao2010 on 5/18/16.
 */
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import io.parallec.core.*;
import io.parallec.core.config.ParallelTaskConfig;
import io.parallec.core.util.PcDateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

/**
 * Sample results visualized in Kibana as in:
 * http://www.parallec.io/images/screenshots/elastic-aggre-web3.png
 *
 * Assuming local elasticsearch-1.3.4  + kibana-3.1.2 running with default basic setup.
 *
 * hitting
 * http://www.parallec.io/validateInternals.html
 * http://www.jeffpei.com/validateInternals.html
 * http://www.restcommander.com/validateInternals.html
 *
 * @author Yuanteng (Jeff) Pei
 */
public class Http3WebAgrregateToElasticSearchMinApp {


    public static void main(String[] args) {

        ParallelClient pc = new ParallelClient();
        org.elasticsearch.node.Node node = nodeBuilder().node(); //elastic client initialize
        HashMap<String, Object> responseContext = new HashMap<String, Object>();
        responseContext.put("Client", node.client());

        ParallelTask task = pc.prepareHttpGet("/validateInternals.html")
                .setTargetHostsFromString("www.parallec.io www.jeffpei.com www.restcommander.com")
                .setResponseContext(responseContext).setConfig(genConfig())
                .execute( new ParallecResponseHandler() {
                    public void onCompleted(ResponseOnSingleTask res,
                                            Map<String, Object> responseContext) {
                        String cpu = new FilterRegex(".*<td>CPU-Usage-Percent</td>\\s*<td>(.*?)</td>[\\s\\S]*")
                                .filter(res.getResponseContent());
                        String memory = new FilterRegex(".*<td>Memory-Used-KB</td>\\s*<td>(.*?)</td>[\\s\\S]*")
                                .filter(res.getResponseContent());
                        Map<String, Object> metricMap = new HashMap<String, Object>();
                        metricMap.put("CpuUsage", cpu); metricMap.put("MemoryUsage", memory);
                        metricMap.put("LastUpdated",PcDateUtils.getNowDateTimeStrStandard());
                        metricMap.put("NodeGroupType", "Web3");
                        System.out.println("cpu:" + cpu + " host: " + res.getHost() );
                        Client client = (Client) responseContext.get("Client");
                        client.prepareIndex("local", "parallec", res.getHost()).setSource(metricMap).execute();
                    }
                });
        String res = task.getRequestNumActual()
                + " Servers in "
                + task.getDurationSec()
                + " seconds. Results: "
                + " Results: "
                + task.getAggregatedResultHumanStr();
        System.out.println("Task summary: \n " + res);
        node.close();
        pc.releaseExternalResources();
    }
    public static ParallelTaskConfig genConfig() {
        ParallelTaskConfig config = new ParallelTaskConfig();
        config.setActorMaxOperationTimeoutSec(20);
        config.setAutoSaveLogToLocal(true);
        config.setSaveResponseToTask(true);
        return config;
    }
}