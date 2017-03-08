package gr.unipi.elastic;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.Operator.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

public class App {
	public static void main(String[] args) throws UnknownHostException {
		try (PreBuiltTransportClient client = new PreBuiltTransportClient(Settings.EMPTY)) {
			client.addTransportAddress( new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			
			//Delete the index
			client.admin().indices().delete(new DeleteIndexRequest("twitter")).actionGet();
			
			//Index using Elasticsearch helpers
			IndexResponse responseIndexJava = client.prepareIndex("twitter", "tweet", "1")
			        .setSource(jsonBuilder()
						    .startObject()
					        .field("user", "kimchy")
					        .field("postDate", new Date())
					        .field("message", "drives out Elasticsearch")
					        .field("favorites", "14")
					    .endObject())
			        .get();
			System.out.println(responseIndexJava);
			
			//Index using JSON
			String json = "{" +
			        "\"user\":\"Alice\"," +
			        "\"postDate\":\"2017-03-09\"," +
			        "\"message\":\"out Elastic\"," +
			        "\"favorites\":\"9\"" +
			    "}";
			IndexResponse responseIndexJson = client.prepareIndex("twitter", "tweet", "2")
			        .setSource(json)
			        .get();
			System.out.println(responseIndexJson);
			
			//Sleep to give time to Elasticsearch
			Thread.sleep(2000);
			
			//Get document from Index
			GetResponse responseGet = client.prepareGet("twitter", "tweet", "1").get();
			System.out.println(responseGet.toString());
			
			//Search for user Alice using Match query
			//Query string gets analyzed
			SearchResponse responseSearch1 = client.prepareSearch("twitter")
			        .setTypes("tweet")
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(matchQuery("user", "Alice"))                 // Match Query
			        .get();
			System.out.println("responseSearch1:\t" + responseSearch1);
			
			//Search for user Alice using Term query
			//Query string does not get analyzed
			SearchResponse responseSearch2 = client.prepareSearch("twitter")
			        .setTypes("tweet")
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(termQuery("user", "Alice"))                 // Term Query
			        .get();
			System.out.println("responseSearch2:\t" + responseSearch2);
			
			//Search for 'out trying' using Match query
			//Does not have to be in the exact order
			SearchResponse responseSearch3 = client.prepareSearch("twitter")
			        .setTypes("tweet")
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(matchQuery("message", "out try"))                 // Match Query
			        .get();
			System.out.println("responseSearch3:\t" + responseSearch3);
			
			//Search for 'out trying' using Match Phrase query
			//It has to be in the exact order
			SearchResponse responseSearch4 = client.prepareSearch("twitter")
			        .setTypes("tweet")
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(matchPhraseQuery("message", "out trying"))        // Match Phrase Query
			        .get();
			System.out.println("responseSearch4:\t" + responseSearch4);
			
			//Search for 'try out' using Match query
			//Stemmer enables the word 
			SearchResponse responseSearch5 = client.prepareSearch("twitter")
			        .setTypes("tweet")
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(matchQuery("message", "drive"))    // Match Query
			        .get();
			System.out.println("responseSearch5:\t" + responseSearch5);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
