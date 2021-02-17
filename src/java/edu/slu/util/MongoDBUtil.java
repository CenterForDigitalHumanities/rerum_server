/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.util;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
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
     *
     * @return
     */
    public static MongoDBUtil getInstance() {
        return instance;
    }

    static {
        try {
            final MongoClientOptions options = MongoClientOptions.builder()
            .connectionsPerHost(100)
            .build();
            MongoClientURI uri = new MongoClientURI(
                    "mongodb://USER:PASS@f-vl-cdh-img-01:27017/DATABASE?w=majority&authMechanism=PICK_ONE"
            );
            mg = new MongoClient(uri);
            db = mg.getDB("annotationStoreDev");
        } catch (Exception e) {
            Logger.getLogger(MongoDBUtil.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * @return the db
     */
    public static DB getDb() {
        return db;
    }

    public Set<String> getCollectionNames() {
        return db.getCollectionNames();
    }

}
