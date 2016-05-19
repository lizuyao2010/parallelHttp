import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Created by lizuyao2010 on 5/18/16.
 */
public class IndexApp {
    public static Node node=nodeBuilder().node();
    public static void main(String[] args)
    {
//        createTwitter();
        getTwitter();
    }
    private static void print(GetResponse response)
    {
        System.out.println(response.getIndex());
        System.out.println(response.getType());
        System.out.println(response.getId());
        System.out.println(response.getVersion());
    }
    private static void print(IndexResponse response)
    {
        System.out.println(response.getIndex());
        System.out.println(response.getType());
        System.out.println(response.getId());
        System.out.println(response.getVersion());
        System.out.println(response.isCreated());
    }
    private static void createTwitter()
    {
        Client client=node.client();
        String json = "{" +
                "\"user\":\"zuyao\"," +
                "\"postDate\":\"2013-01-31\"," +
                "\"message\":\"trying out Elasticsearch version 2\"" +
                "}";

        IndexResponse response = client.prepareIndex("twitter", "tweet","1")
                .setSource(json)
                .get();
        print(response);
        node.close();
    }
    private static void getTwitter()
    {
        Client client=node.client();
        GetResponse response = client.prepareGet("twitter", "tweet", "1").get();
//        GetResponse response = client.prepareGet("local", "parallec", "www.parallec.io").get();
        print(response);
        node.close();
    }
}
