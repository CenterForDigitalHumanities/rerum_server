/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.abstractUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import edu.slu.common.Constant;
import edu.slu.util.MongoDBUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * @author hanyan
 */
public abstract class MongoDBAbstractDAO implements MongoDBDAOInterface {

    private final MongoDatabase db = MongoDBUtil.getDb();
    
    //private static MongoClient mg = null;
    //private static MongoDatabase db = null;
    
    @Override
    public List<DBObject> findAll(String collectionName, int firstResult, int maxResults) {
        ArrayList results;
        results = db.getCollection(collectionName)
                .find()
                .skip(firstResult)
                .limit(maxResults)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findAll(String collectionName) {
        ArrayList results;
        results = db.getCollection(collectionName)
                .find()
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findAllWithOrder(String collectionName, int firstResult, int maxResults, DBObject orderby) {
        ArrayList results;
        results = db.getCollection(collectionName)
                .find()
                .sort((Bson) orderby)
                // This is not how it is in the documentation so I'm keeping the input type the same...
                .skip(firstResult)
                .limit(maxResults)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExample(String collectionName, DBObject queryEntity) {
        ArrayList results;
        results = db.getCollection(collectionName)
                .find((Bson) queryEntity)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExample(String collectionName, DBObject queryEntity, int firstResult, int maxResults) {
        ArrayList results;
        results = db.getCollection(collectionName)
                .find((Bson) queryEntity)
                .skip(firstResult)
                .limit(maxResults)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions) {
        ArrayList results;
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(e -> {
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        });
        results = db.getCollection(collectionName).find((Bson) dbo)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, DBObject orderby) {
        ArrayList results;
        BasicDBObject query = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        conditions.entrySet().stream().map(e -> {
            BasicDBObject dbo = new BasicDBObject();
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
            return dbo;
        }).forEachOrdered(dbo -> {
            values.add(dbo);
        });
        query.put(OROperator, values);
        results = db.getCollection(collectionName)
                .find((Bson) query)
                .sort((Bson) orderby)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhere(String collectionName, Map<String, String> conditions) {
        ArrayList results;
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(e -> {
            Pattern p = Pattern.compile(e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        });
        results = (ArrayList) db.getCollection(collectionName).find((Bson) dbo);
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromPattern(String collectionName, Map<String, Pattern> m_pattern) {
        ArrayList results;
        BasicDBObject dbo = new BasicDBObject();
        m_pattern.entrySet().forEach(p -> {
            dbo.put(p.getKey(), p.getValue());
        });
        results = (ArrayList) db.getCollection(collectionName).find((Bson) dbo);
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrder(String collectionName, Map<String, String> conditions, DBObject orderby) {
        ArrayList results;
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(e -> {
            Pattern p = Pattern.compile(e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        });
        results = db.getCollection(collectionName)
                .find((Bson) dbo)
                .sort((Bson) orderby)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName, Map<String, Pattern> m_pattern, DBObject orderby) {
        ArrayList results;
        BasicDBObject dbo = new BasicDBObject();
        for (Map.Entry<String, Pattern> p : m_pattern.entrySet()) {
            dbo.put(p.getKey(), p.getValue());
        }
        results = db.getCollection(collectionName)
                .find((Bson) dbo)
                .sort((Bson) orderby)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName,
            Map<String, Pattern> m_pattern, String OROperator, DBObject orderby) {
        ArrayList results;
        BasicDBObject query = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        m_pattern.entrySet().stream().map(p -> {
            BasicDBObject dbo = new BasicDBObject();
            dbo.put(p.getKey(), p.getValue());
            return dbo;
        }).forEachOrdered(dbo -> {
            values.add(dbo);
        });
        query.put(OROperator, values);
        results = db.getCollection(collectionName)
                .find((Bson) query)
                .sort((Bson)orderby)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, int firstResult, int maxResult) {
        ArrayList results;
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(e -> {
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        });
        results = db.getCollection(collectionName)
                .find((Bson) dbo)
                .skip(firstResult)
                .limit(maxResult)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, int firstResult, int maxResult) {
        ArrayList results;
        BasicDBObject query = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        conditions.entrySet().stream().map(e -> {
            BasicDBObject dbo = new BasicDBObject();
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
            return dbo;
        }).forEachOrdered(dbo -> {
            values.add(dbo);
        });
        query.put(OROperator, values);
        results = db.getCollection(collectionName)
                .find((Bson) query)
                .skip(firstResult)
                .limit(maxResult)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public List<DBObject> findByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject orderBy) {
        ArrayList results;
        results = db.getCollection(collectionName)
                .find((Bson)queryEntity)
                .sort((Bson)orderBy)
                .into(new ArrayList<>());
        return results;
    }

    @Override
    public DBObject findOneByExample(String collectionName, DBObject queryEntity) {
        Document result = db.getCollection(collectionName).find((Bson)queryEntity).limit(1).first();
        return new BasicDBObject(result);
    }

    @Override
    public DBObject findOneByExampleCaseInsesitive(String collectionName, Map<String, String> conditions) {
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(s -> {
            Pattern p = Pattern.compile(s.getValue(), CASE_INSENSITIVE);
            dbo.put(s.getKey(), p);
        });
        Document result = db.getCollection(collectionName).find((Bson) dbo).limit(1).first();
        return new BasicDBObject(result);
    }

    @Override
    public DBObject findOneByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject returnFields, DBObject orderBy) {
        Document result = db.getCollection(collectionName).find((Bson)queryEntity)
                .projection(Projections.fields((Bson)returnFields))
                .sort((Bson)orderBy)
                .first();
        return new BasicDBObject(result);
    }

    @Override
    public void delete(String collectionName, DBObject queryEntity) {
        MongoCollection coll = db.getCollection(collectionName);
        coll.deleteOne((Bson)queryEntity);
    }

    @Override
    public void update(String collectionName, DBObject queryEntity, DBObject targetEntity) {
        MongoCollection coll = db.getCollection(collectionName);
        coll.updateOne((Bson)queryEntity, (Bson)targetEntity);
    }

    /**
     * @param collectionName
     * @param targetEntity
     * @return ObjectID.toString()
     */
    @Override
    public String save(String collectionName, DBObject targetEntity) {
        System.out.println("Overwritten mong save called");
        System.out.println("Collection to find in MongoDB is "+collectionName);
        MongoCollection coll = db.getCollection(collectionName);
        System.out.println("Found the collection");
        String generatedID = new ObjectId().toHexString(); //Should always be a hex string for our purposes.
        //If you do not explicitly create a new objectID hexidecimal string here, it could be a date.
        targetEntity.put("_id", generatedID);
        System.out.println("insert into collection...");
        coll.insertOne(targetEntity);
        System.out.println("insert complete.  Resulting ID is "+generatedID);
        return generatedID;
    }

    @Override
    public JSONArray bulkSaveMetadataForm(String collectionName, BasicDBList entity_array) {
        MongoCollection coll = db.getCollection(collectionName);
        DBObject[] listAsObj = new DBObject[entity_array.size()];
        for (int i = 0; i < entity_array.size(); i++) {
            DBObject objectToAdd = (DBObject) entity_array.get(i);
            coll.insertOne(objectToAdd);
            listAsObj[i] = objectToAdd;
        }
        return bulkSetIDProperty(collectionName, listAsObj);
    }

    /* Bulk save objects into collection */
    @Override
    public JSONArray bulkSaveFromCopy(String collectionName, BasicDBList entity_array) {
        MongoCollection coll = db.getCollection(collectionName);
        DBObject[] listAsObj = new DBObject[entity_array.size()];
        for (int i = 0; i < entity_array.size(); i++) {
            DBObject objectToAdd = (DBObject) entity_array.get(i);
            String generatedID = new ObjectId().toHexString(); //Should always be a hex string for our purposes.
            //If you do not explicitly create a new objectID hexidecimal string here, it could be a date.
            objectToAdd.put("_id", generatedID);
            listAsObj[i] = objectToAdd;
        }
        coll.insertOne(listAsObj);
        return bulkSetIDProperty(collectionName, listAsObj);
    }

    /* Go through each newly copied object and update its @id property */
    @Override
    public JSONArray bulkSetIDProperty(String collectionName, DBObject[] entity_array) {
        int size = entity_array.length;
        MongoCollection coll = db.getCollection(collectionName);
        JSONArray listAsArr = new JSONArray();
        for (int j = 0; j < size; j++) {
            DBObject targetEntity = (DBObject) entity_array[j];
            targetEntity.put("@id", Constant.RERUM_ID_PREFIX + (targetEntity.get("_id").toString()));
            entity_array[j] = targetEntity; //update this so the updated object can be returned
            listAsArr.add(targetEntity);
            DBObject findThis = new BasicDBObject();
            findThis.put("_id", targetEntity.get("_id").toString());
            coll.updateOne((Bson)findThis, (Bson)targetEntity);
        }
        return listAsArr;
    }

    public Long count(String collectionName) {
        MongoCollection coll = db.getCollection(collectionName);
        return coll.countDocuments();
    }

    public Long count(String collectionName, DBObject queryEntity) {
        MongoCollection coll = db.getCollection(collectionName);
        return coll.countDocuments((Bson)queryEntity);
    }
}
