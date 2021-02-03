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
            System.out.println("Establishing Mongo Client to mongodb://f-vl-cdh-img-01:27017...");
            final MongoClientOptions options = MongoClientOptions.builder()
                    .connectionsPerHost(100)
                    .build();
            MongoClientURI uri = new MongoClientURI("mongodb://f-vl-cdh-img-01:27017/?retryWrites=true&w=majority");
            mg = new MongoClient(uri);
            System.out.println("... Mongo Client Established");
            System.out.println("Establishing annotationStore database connection...");
            db = mg.getDB("annotationStore");
            System.out.println("Database is AnnotationStore");
        } catch (Exception e) {
            System.out.println("!!! Mongo Client FAIL !!!");
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
