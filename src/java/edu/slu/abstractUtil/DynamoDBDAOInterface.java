/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.abstractUtil;


import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import net.sf.json.JSONObject;

/**
 *
 * @author shrav
 */
public interface DynamoDBDAOInterface {
    public Item find(String rerum_id);
    public String save(String id, JSONObject jobj);
    public void update(String id, JSONObject jobj);
    public void delete(String id);
    
}
