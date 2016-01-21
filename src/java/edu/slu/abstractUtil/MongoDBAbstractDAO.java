/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.abstractUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import edu.slu.util.MongoDBUtil;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author hanyan
 */
public abstract class MongoDBAbstractDAO implements MongoDBDAOInterface {
    private DB db = MongoDBUtil.getDb();
    
    public List<DBObject> findAll(String collectionName, int firstResult, int maxResults){
        DBCollection coll = db.getCollection(collectionName);
        DBCursor results = coll.find();
        return results.skip(firstResult).limit(maxResults).toArray();
    }

    public List<DBObject> findAll(String collectionName){
        DBCollection coll = db.getCollection(collectionName);
        DBCursor results = coll.find();
        return results.toArray();
    }
    
    public List<DBObject> findAllWithOrder(String collectionName, int firstResult, int maxResults, DBObject orderby){
        DBCollection coll = db.getCollection(collectionName);
        DBCursor results = coll.find().sort(orderby);
        return results.skip(firstResult).limit(maxResults).toArray();
    }
    
    public List<DBObject> findByExample(String collectionName, DBObject queryEntity){
        DBCollection coll = db.getCollection(collectionName);
        DBCursor results = coll.find(queryEntity);
        return results.toArray();
    }

    public List<DBObject> findByExample(String collectionName, DBObject queryEntity, int firstResult, int maxResults){
        DBCollection coll = db.getCollection(collectionName);
        DBCursor results = coll.find(queryEntity).skip(firstResult).limit(maxResults);
        return results.toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject dbo = new BasicDBObject();
        for(Map.Entry<String, String> e : conditions.entrySet()){
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        }
        DBCursor results = coll.find(dbo);
        return results.toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, DBObject orderby){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        for(Map.Entry<String, String> e : conditions.entrySet()){
            BasicDBObject dbo = new BasicDBObject();
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
            values.add(dbo);
        }
        query.put(OROperator, values);
        DBCursor results = coll.find(query).sort(orderby);
        return results.toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhere(String collectionName, Map<String, String> conditions){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject dbo = new BasicDBObject();
        for(Map.Entry<String, String> e : conditions.entrySet()){
            Pattern p = Pattern.compile(e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        }
        DBCursor results = coll.find(dbo);
        return results.toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhereFromPattern(String collectionName, Map<String, Pattern> m_pattern){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject dbo = new BasicDBObject();
        for(Map.Entry<String, Pattern> p : m_pattern.entrySet()){
            dbo.put(p.getKey(), p.getValue());
        }
        DBCursor results = coll.find(dbo);
        return results.toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhereWithOrder(String collectionName, Map<String, String> conditions, DBObject orderby){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject dbo = new BasicDBObject();
        for(Map.Entry<String, String> e : conditions.entrySet()){
            Pattern p = Pattern.compile(e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        }
        DBCursor results = coll.find(dbo).sort(orderby);
        return results.toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName, Map<String, Pattern> m_pattern, DBObject orderby){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject dbo = new BasicDBObject();
        for(Map.Entry<String, Pattern> p : m_pattern.entrySet()){
            dbo.put(p.getKey(), p.getValue());
        }
        DBCursor results = coll.find(dbo).sort(orderby);
        return results.toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName, 
            Map<String, Pattern> m_pattern, String OROperator, DBObject orderby){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        for(Map.Entry<String, Pattern> p : m_pattern.entrySet()){
            BasicDBObject dbo = new BasicDBObject();
            dbo.put(p.getKey(), p.getValue());
            values.add(dbo);
        }
        query.put(OROperator, values);
        DBCursor results = coll.find(query).sort(orderby);
        return results.toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, int firstResult, int maxResult){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject dbo = new BasicDBObject();
        for(Map.Entry<String, String> e : conditions.entrySet()){
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
        }
        DBCursor results = coll.find(dbo);
        return results.skip(firstResult).limit(maxResult).toArray();
    }
    
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, int firstResult, int maxResult){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject query = new BasicDBObject();
        BasicDBList values = new BasicDBList();
        for(Map.Entry<String, String> e : conditions.entrySet()){
            BasicDBObject dbo = new BasicDBObject();
            Pattern p = Pattern.compile("^" + e.getValue(), CASE_INSENSITIVE);
            dbo.put(e.getKey(), p);
            values.add(dbo);
        }
        query.put(OROperator, values);
        DBCursor results = coll.find(query);
        return results.skip(firstResult).limit(maxResult).toArray();
    }
    
    public List<DBObject> findByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject orderBy){
        DBCollection coll = db.getCollection(collectionName);
        return coll.find(queryEntity).sort(orderBy).toArray();
    }

    public DBObject findOneByExample(String collectionName, DBObject queryEntity){
        DBCollection coll = db.getCollection(collectionName);
        return coll.findOne(queryEntity);
    }
    
    public DBObject findOneByExampleCaseInsesitive(String collectionName, Map<String, String> conditions){
        DBCollection coll = db.getCollection(collectionName);
        BasicDBObject dbo = new BasicDBObject();
        for(Map.Entry<String, String> s : conditions.entrySet()){
            Pattern p = Pattern.compile(s.getValue(), CASE_INSENSITIVE);
            dbo.put(s.getKey(), p);
        }
        return coll.findOne(dbo);
    }
    
    public DBObject findOneByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject returnFields, DBObject orderBy){
        DBCollection coll = db.getCollection(collectionName);
        return coll.findOne(queryEntity, returnFields, orderBy);
    }
    
    public void delete(String collectionName, DBObject queryEntity){
        DBCollection coll = db.getCollection(collectionName);
        coll.remove(queryEntity);
    }
    
    public void update(String collectionName, DBObject queryEntity, DBObject targetEntity){
        DBCollection coll = db.getCollection(collectionName);
        coll.update(queryEntity, targetEntity);
    }
    
    /**
     * @param collectionName
     * @param targetEntity
     * @return ObjectID.toString()
     */
    public String save(String collectionName, DBObject targetEntity){
        DBCollection coll = db.getCollection(collectionName);
        coll.save(targetEntity);
        return targetEntity.get("_id").toString();
    }
    
    
    public Long count(String collectionName){
        DBCollection coll = db.getCollection(collectionName);
        return coll.count();
    }
    
    public Long count(String collectionName, DBObject queryEntity){
        DBCollection coll = db.getCollection(collectionName);
        return coll.count(queryEntity);
    }
}
