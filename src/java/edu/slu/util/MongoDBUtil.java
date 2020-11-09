/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hanyan
 */
public class MongoDBUtil {

    private static MongoClient mg = null;
    private static MongoDatabase db = null;
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
            ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
            mg = new MongoClient(serverAddress, options);
            MongoDatabase dbAuth = mg.getDatabase("admin");
//            if(!auth){
//                auth = dbAuth.authenticate("root", "root".toCharArray());
            db = mg.getDatabase("annotationStore");
//            }
        } catch (Exception e) {
            Logger.getLogger(MongoDBUtil.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * @return the db
     */
    public static MongoDatabase getDb() {
        return db;
    }

    public Set<String> getCollectionNames() {
        ArrayList<String> results = db.listCollectionNames()
                .into(new ArrayList<>());
        return new LinkedHashSet<>(results);
    }

}
