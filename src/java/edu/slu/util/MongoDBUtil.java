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
            System.out.println("MongoDBUTIL");
            final MongoClientOptions options = MongoClientOptions.builder()
                    .connectionsPerHost(100)
                    .build();
            System.out.println("MongoDBUTIL - Connection");
            MongoClientURI uri = new MongoClientURI(
                    "mongodb+srv://rerumBot:f%40kePassword@cluster0.qytdr.mongodb.net/annotationStore?retryWrites=true&w=majority");
            mg = new MongoClient(uri);
            System.out.println("MongoDBUTIL - Connected");
            System.out.println("Get DB");
            db = mg.getDB("annotationStore");
            System.out.println("Got the DB...hoping it is DB 'annotationStore' with db.alpha collection");
            System.out.println("DB is "+db.getName());
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
