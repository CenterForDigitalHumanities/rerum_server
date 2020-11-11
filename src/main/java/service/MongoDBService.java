/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;
import com.mongodb.BasicDBList;
import abstractUtil.MongoDBDAOInterface;
import net.sf.json.JSONArray;


/**
 *
 * @author hanyan
 */
public interface MongoDBService extends MongoDBDAOInterface {

    public JSONArray bulkSaveMetadataForm(String COLLECTION_ANNOTATION, BasicDBList dbo);

    
    
}
