/**
 * Created by lizuyao2010 on 5/19/16.
 */
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static spark.Spark.*;
public class Server {
    public static void main(String[] args) {
        Gson gson = new Gson();
        Metrics metrics = new Metrics();
        Map<String,String> cpu=new HashMap<>();
        cpu.put("core1","80%");
        cpu.put("core2","33%");
        metrics.setCpu(cpu);
        Map<String,String> mem=new HashMap<>();
        mem.put("used","1234M");
        mem.put("free","6666M");
        metrics.setMem(mem);
        get("/metric", (request, response) -> metrics, gson::toJson);

    }
}
