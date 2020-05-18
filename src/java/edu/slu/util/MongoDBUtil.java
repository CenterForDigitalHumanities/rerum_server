/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.util;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoOptions;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hanyan
 */
public class MongoDBUtil {
    private static MongoClient mg = null;
    private static DB db = null;
    private static boolean auth = false;

    private final static MongoDBUtil instance = new MongoDBUtil();

    /**
     * Instantiate
     * @return
     * @throws Exception
     */
     public static MongoDBUtil getInstance() {
            return instance;
     }
    
    static {
        try {
            mg = new MongoClient("165.134.107.94", 27017);
            DB dbAuth = mg.getDB("admin");
//            if(!auth){
//                auth = dbAuth.authenticate("root", "root".toCharArray());
                db = mg.getDB("annotationStore");
                MongoOptions options = mg.getMongoOptions();
                options.autoConnectRetry = true;
                options.connectionsPerHost = 100;
//            }
        } catch (Exception e) {
            Logger.getLogger(MongoDBUtil.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }
    }

    /**
     * @return the db
     */
    public static DB getDb() {
        return db;
    }
    
    public Set<String> getCollectionNames(){
        Set<String> results = null;
        results = db.getCollectionNames();
        return results;
    }
           
}
