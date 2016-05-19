import java.io.Serializable;
import java.util.Map;

/**
 * Created by lizuyao2010 on 5/19/16.
 */
public class Metrics implements Serializable {
    private Map<String,String> cpu;
    private Map<String,String> mem;

    public Map<String, String> getMem() {
        return mem;
    }

    public void setMem(Map<String, String> mem) {
        this.mem = mem;
    }

    public Map<String, String> getCpu() {
        return cpu;
    }

    public void setCpu(Map<String, String> cpu) {
        this.cpu = cpu;
    }
}
