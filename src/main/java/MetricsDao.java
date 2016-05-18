/**
 * Created by lizuyao2010 on 5/18/16.
 */

import java.security.Timestamp;

/**
 * Responsible for persistence of metric data
 */
public interface MetricsDao {
    /**
     *
     * @param metrics the JSON string representing the Metric collected
     * @param timestamp time of the collection of the metric
     * @throws RuntimeException or subclasses representing exceptions
     */
    public void storeMetrics(String metrics, Timestamp timestamp);
}
