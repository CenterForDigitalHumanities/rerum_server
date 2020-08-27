/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.abstractUtil;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.simple.JSONObject;
import jdk.nashorn.internal.parser.JSONParser;
import com.fasterxml.jackson.core.JsonParser;

/**
 *
 * @author shrav
 */
public abstract class DynamoDBAbstractDAO implements DynamoDBDAOInterface {

    private Item item;
    private String json_obj;
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String tableName = "rerum-dev";
    Table table = dynamoDB.getTable(tableName);
    
    /**
     *
     * @param rerum_id
     * @return item
     */

    public Item find(String rerum_id) {

        item = table.getItem("id", rerum_id);
        return item;
    }
    /**
     *
     * @param id
     * @param jobj
     * @return json_obj
     */
    public String save(String id, JSONObject jobj) {
        Item item = new Item().withPrimaryKey("id", id)
                .withJSON("JSONObj", jobj.toJSONString());
        table.putItem(item);
        item = table.getItem("id", id);
        json_obj = item.toJSON();
        return (json_obj);
    }
    /**
     * @param id
     * @param jobj
     */
    public void update(String id, JSONObject jobj) {
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", id).
                withAttributeUpdate(new AttributeUpdate("JSONObj").put(jobj.toJSONString()));;
    }
    /**
     * 
     * @param id 
     */
    public void delete(String id) {
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey("id", id);
        DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);
    }

}
