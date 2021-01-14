/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.abstractUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import edu.slu.common.Constant;
import edu.slu.util.MongoDBUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import net.sf.json.JSONArray;
import org.bson.types.ObjectId;

/**
 * @author hanyan
 */
public abstract class MongoDBAbstractDAO implements MongoDBDAOInterface {

    private final DB db = MongoDBUtil.getDb();
    
    //private static MongoClient mg = null;
    //private static MongoDatabase db = null;
    
    @Override
    public List<DBObject> findAll(String collectionName, int firstResult, int maxResults) {
        List<DBObject> results;
        results = db.getCollection(collectionName)
                .find()
                .skip(firstResult)
                .limit(maxResults)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findAll(String collectionName) {
        List<DBObject> results;
        results = db.getCollection(collectionName)
                .find()
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findAllWithOrder(String collectionName, int firstResult, int maxResults, DBObject orderby) {
        List<DBObject> results;
        results = db.getCollection(collectionName)
                .find()
                .sort(orderby)
                // This is not how it is in the documentation so I'm keeping the input type the same...
                .skip(firstResult)
                .limit(maxResults)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExample(String collectionName, DBObject queryEntity) {
        List<DBObject> results;
        results = db.getCollection(collectionName)
                .find(queryEntity)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExample(String collectionName, DBObject queryEntity, int firstResult, int maxResults) {
        List<DBObject> results;
        results = db.getCollection(collectionName)
                .find(queryEntity)
                .skip(firstResult)
                .limit(maxResults)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions) {
        List<DBObject> results;
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(e -> {
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        });
        results = db.getCollection(collectionName).find(dbo)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, DBObject orderby) {
        List<DBObject> results;
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
                .find(query)
                .sort(orderby)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhere(String collectionName, Map<String, String> conditions) {
        List<DBObject> results;
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(e -> {
            Pattern p = Pattern.compile(e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        });
        results = db.getCollection(collectionName).find(dbo).toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromPattern(String collectionName, Map<String, Pattern> m_pattern) {
        List<DBObject> results;
        BasicDBObject dbo = new BasicDBObject();
        m_pattern.entrySet().forEach(p -> {
            dbo.put(p.getKey(), p.getValue());
        });
        results = (ArrayList) db.getCollection(collectionName).find(dbo).toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrder(String collectionName, Map<String, String> conditions, DBObject orderby) {
        List<DBObject> results;
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(e -> {
            Pattern p = Pattern.compile(e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        });
        results = db.getCollection(collectionName)
                .find(dbo)
                .sort(orderby)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName, Map<String, Pattern> m_pattern, DBObject orderby) {
        List<DBObject> results;
        BasicDBObject dbo = new BasicDBObject();
        for (Map.Entry<String, Pattern> p : m_pattern.entrySet()) {
            dbo.put(p.getKey(), p.getValue());
        }
        results = db.getCollection(collectionName)
                .find(dbo)
                .sort(orderby)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName,
            Map<String, Pattern> m_pattern, String OROperator, DBObject orderby) {
        List<DBObject> results;
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
                .find(query)
                .sort(orderby)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, int firstResult, int maxResult) {
        List<DBObject> results;
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(e -> {
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        });
        results = db.getCollection(collectionName)
                .find(dbo)
                .skip(firstResult)
                .limit(maxResult)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, int firstResult, int maxResult) {
        List<DBObject> results;
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
                .find(query)
                .skip(firstResult)
                .limit(maxResult)
                .toArray();
        return results;
    }

    @Override
    public List<DBObject> findByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject orderBy) {
        List<DBObject> results;
        results = db.getCollection(collectionName)
                .find(queryEntity)
                .sort(orderBy)
                .toArray();
        return results;
    }

    @Override
    public DBObject findOneByExample(String collectionName, DBObject queryEntity) {
        Map result = db.getCollection(collectionName).find(queryEntity).limit(1).one().toMap();
        return new BasicDBObject(result);
    }

    @Override
    public DBObject findOneByExampleCaseInsesitive(String collectionName, Map<String, String> conditions) {
        BasicDBObject dbo = new BasicDBObject();
        conditions.entrySet().forEach(s -> {
            Pattern p = Pattern.compile(s.getValue(), CASE_INSENSITIVE);
            dbo.put(s.getKey(), p);
        });
        //FIXME probably a better way to do this
        Map result = db.getCollection(collectionName).find(dbo).limit(1).one().toMap();
        return new BasicDBObject(result);
    }
    
    /**
     * UNUSED
     * This one was tough to refactor for new mongo, not sure about projections. 
     */
    
    @Override
    public DBObject findOneByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject returnFields, DBObject orderBy) {
        //Document result = db.getCollection(collectionName).find(queryEntity)
        //        .projection(Projections.fields((Bson)returnFields))
        //        .sort((Bson)orderBy)
        //        .first();
        return new BasicDBObject("hello", "world");
    }

    @Override
    public void delete(String collectionName, DBObject queryEntity) {
        DBCollection coll = db.getCollection(collectionName);
        coll.remove(queryEntity);
    }

    @Override
    public void update(String collectionName, DBObject queryEntity, DBObject targetEntity) {
        DBCollection coll = db.getCollection(collectionName);
        coll.update(queryEntity, targetEntity);
    }

    /**
     * @param collectionName
     * @param targetEntity
     * @return ObjectID.toString()
     */
    @Override
    public String save(String collectionName, DBObject targetEntity) {
        DBCollection coll = db.getCollection(collectionName);
        String generatedID = new ObjectId().toHexString(); //Should always be a hex string for our purposes.
        //If you do not explicitly create a new objectID hexidecimal string here, it could be a date.
        targetEntity.put("_id", generatedID);
        coll.insert(targetEntity);
        return generatedID;
    }

    @Override
    public JSONArray bulkSaveMetadataForm(String collectionName, BasicDBList entity_array) {
        DBCollection coll = db.getCollection(collectionName);
        DBObject[] listAsObj = new DBObject[entity_array.size()];
        for (int i = 0; i < entity_array.size(); i++) {
            DBObject objectToAdd = (DBObject) entity_array.get(i);
            coll.insert(objectToAdd);
            listAsObj[i] = objectToAdd;
        }
        return bulkSetIDProperty(collectionName, listAsObj);
    }

    /* Bulk save objects into collection */
    @Override
    public JSONArray bulkSaveFromCopy(String collectionName, BasicDBList entity_array) {
        DBCollection coll = db.getCollection(collectionName);
        DBObject[] listAsObj = new DBObject[entity_array.size()];
        for (int i = 0; i < entity_array.size(); i++) {
            DBObject objectToAdd = (DBObject) entity_array.get(i);
            String generatedID = new ObjectId().toHexString(); //Should always be a hex string for our purposes.
            //If you do not explicitly create a new objectID hexidecimal string here, it could be a date.
            objectToAdd.put("_id", generatedID);
            listAsObj[i] = objectToAdd;
        }
        coll.insert(listAsObj);
        return bulkSetIDProperty(collectionName, listAsObj);
    }

    /* Go through each newly copied object and update its @id property */
    @Override
    public JSONArray bulkSetIDProperty(String collectionName, DBObject[] entity_array) {
        int size = entity_array.length;
        DBCollection coll = db.getCollection(collectionName);
        JSONArray listAsArr = new JSONArray();
        for (int j = 0; j < size; j++) {
            DBObject targetEntity = (DBObject) entity_array[j];
            targetEntity.put("@id", Constant.RERUM_ID_PREFIX + (targetEntity.get("_id").toString()));
            entity_array[j] = targetEntity; //update this so the updated object can be returned
            listAsArr.add(targetEntity);
            DBObject findThis = new BasicDBObject();
            findThis.put("_id", targetEntity.get("_id").toString());
            coll.update(findThis, targetEntity);
        }
        return listAsArr;
    }

    public Long count(String collectionName) {
        DBCollection coll = db.getCollection(collectionName);
        return coll.count();
    }

    public Long count(String collectionName, DBObject queryEntity) {
        DBCollection coll = db.getCollection(collectionName);
        return coll.count(queryEntity);
    }
}
