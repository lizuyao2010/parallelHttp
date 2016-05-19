import io.parallec.core.FilterRegex;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizuyao2010 on 5/18/16.
 */
public class App {
    protected static final Logger logger = LoggerFactory
            .getLogger(App.class);
    public static void main(String[] args)
    {
        ParallelClient pc = new ParallelClient();

        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc
                .prepareHttpGet("/validateInternals.html")
                .setConcurrency(10000)
                .handleInWorker()
                .setTargetHostsFromString("www.parallec.io www.jeffpei.com www.restcommander.com")
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                                            Map<String, Object> responseContext) {
                        String jsonStr="{\n" +
                                "    \"cpu\": {\n" +
                                "        \"core1\": \"80%\",\n" +
                                "        \"core2\": \"33%\"\n" +
                                "    },\n" +
                                "    \"mem\": {\n" +
                                "        \"used\": \"1234M\",\n" +
                                "        \"free\": \"6666M\"\n" +
                                "    }\n" +
                                "}";

                        String cpu = new FilterRegex(
                                ".*<td>CPU-Usage-Percent</td>\\s*<td>(.*?)</td>.*")
                                .filter(res.getResponseContent());
                        String memory = new FilterRegex(
                                ".*<td>Memory-Used-KB</td>\\s*<td>(.*?)</td>.*")
                                .filter(res.getResponseContent());

                        Map<String, Object> metricMap = new HashMap<String, Object>();
                        metricMap.put("CpuUsage", cpu);
                        metricMap.put("MemoryUsage", memory);

                        logger.info("cpu:" + cpu + " memory: " + memory
                                + " host: " + res.getHost());
                        responseContext.put(res.getHost(), cpu);
//                        logger.debug(res.toString());

                    }
                });
        for (Object o : responseContext.values()) {
            Double cpuDouble = Double.parseDouble((String) o);
//            System.out.println("cpuDouble: "+cpuDouble);
            logger.info("cpuDouble: "+o.toString());
            Asserts.check(cpuDouble <= 100.0 && cpuDouble >= 0.0,
                    " Fail to extract cpu values");
        }
        pc.releaseExternalResources();
    }
}
