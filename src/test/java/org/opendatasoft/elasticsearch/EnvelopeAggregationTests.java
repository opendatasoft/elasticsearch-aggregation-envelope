package org.opendatasoft.elasticsearch;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.mocksocket.MockSocket;
import org.elasticsearch.test.ESIntegTestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class EnvelopeAggregationTests extends ESIntegTestCase {
    /*
    public void testExample() throws Exception {
        String stringAddress = Objects.requireNonNull(System.getProperty("external.address"));
        URL url = new URL("http://" + stringAddress);
        InetAddress address = InetAddress.getByName(url.getHost());
        try (Socket socket = new MockSocket(address, url.getPort());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            assertEquals("TEST", reader.readLine());
        }
    }
    */

    public void testExample2() throws Exception {
        AdminClient ac = admin();
        ClusterHealthResponse chr = ac.cluster().health(new ClusterHealthRequest()).actionGet();
        //System.out.println(chr.getClusterName());
    }

    public void testLoadData() throws Exception {
        Client client = client();

        IndexResponse response = client.prepareIndex("test", "doc")
                .setSource(jsonBuilder()
                    .startObject()
                        .field("user", "user1")
                        .startObject("userData")
                            .field("property1", "value1")
                            .field("property2", "value2")
                        .endObject()
                    .endObject()
                ).setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
                .get();

        String _id = response.getId();
        //System.out.println(_id);

        GetResponse response2 = client.prepareGet("test", "doc", _id).get();
        //System.out.println(response2);
        assertEquals("user1", response2.getSource().get("user"));

        ensureGreen();
        //Thread.sleep(1000);

        SearchResponse response3 = client.prepareSearch("test")
                .setQuery(QueryBuilders.matchAllQuery())
                .get();
        //System.out.println(response3);
        assertEquals(1, response3.getHits().totalHits);
    }

}
