/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.service;
import com.mongodb.BasicDBList;
import edu.slu.abstractUtil.MongoDBDAOInterface;
import net.sf.json.JSONArray;


/**
 *
 * @author hanyan
 */
public interface MongoDBService extends MongoDBDAOInterface {

    public JSONArray bulkSaveMetadataForm(String COLLECTION_ANNOTATION, BasicDBList dbo);

    
    
}
