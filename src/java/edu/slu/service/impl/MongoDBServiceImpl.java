/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.service.impl;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import edu.slu.DAO.MongoDBDAO;
import edu.slu.service.MongoDBService;
import net.sf.json.JSONArray;

/**
 *
 * @author hanyan
 */
public class MongoDBServiceImpl implements MongoDBService { 
    private MongoDBDAO mongoDBDAO;
    
    @Override
    public List<DBObject> findAll(String collectionName, int firstResult, int maxResults) {
        return mongoDBDAO.findAll(collectionName, firstResult, maxResults);
    }

    @Override
    public List<DBObject> findAll(String collectionName) {
        return mongoDBDAO.findAll(collectionName);
    }
    
    @Override
    public List<DBObject> findAllWithOrder(String collectionName, int firstResult, int maxResults, DBObject orderby) {
        return this.mongoDBDAO.findAllWithOrder(collectionName, firstResult, maxResults, orderby);
    }

    @Override
    public List<DBObject> findByExample(String collectionName, DBObject queryEntity) {
        return mongoDBDAO.findByExample(collectionName, queryEntity);
    }

    @Override
    public List<DBObject> findByExample(String collectionName, DBObject queryEntity, int firstResult, int maxResults) {
        return mongoDBDAO.findByExample(collectionName, queryEntity, firstResult, maxResults);
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions) {
        return mongoDBDAO.findByExampleLikeAnyWhereFromHead(collectionName, conditions);
    }
    
    @Override
    public List<DBObject> findByExampleLikeAnyWhere(String collectionName, Map<String, String> conditions){
        return mongoDBDAO.findByExampleLikeAnyWhere(collectionName, conditions);
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, int firstResult, int maxResult) {
        return mongoDBDAO.findByExampleLikeAnyWhereFromHead(collectionName, conditions, firstResult, maxResult);
    }

    @Override
    public List<DBObject> findByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject orderBy) {
        return mongoDBDAO.findByExampleWithOrder(collectionName, queryEntity, orderBy);
    }

    @Override
    public DBObject findOneByExample(String collectionName, DBObject entity) {
        return mongoDBDAO.findOneByExample(collectionName, entity);
    }

    @Override
    public DBObject findOneByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject returnFields, DBObject orderBy) {
        return mongoDBDAO.findOneByExampleWithOrder(collectionName, queryEntity, returnFields, orderBy);
    }

    @Override
    public void delete(String collectionName, DBObject entity) {
        mongoDBDAO.delete(collectionName, entity);
    }

    @Override
    public void update(String collectionName, DBObject queryEntity, DBObject targetEntity) {
        mongoDBDAO.update(collectionName, queryEntity, targetEntity);
    }

    @Override
    public String save(String collectionName, DBObject targetEntity) {
        return mongoDBDAO.save(collectionName, targetEntity);
    }
    
//    @Override
//    public String save(String collectionName, BasicDBList targetEntity) {
//        return mongoDBDAO.save(collectionName, targetEntity);
//    }
    
    @Override
    public Long count(String collectionName) {
        return mongoDBDAO.count(collectionName);
    }

    @Override
    public Long count(String collectionName, DBObject queryEntity) {
        return mongoDBDAO.count(collectionName, queryEntity);
    }

    /**
     * @return the mongoDBDAO
     */
    public MongoDBDAO getMongoDBDAO() {
        return mongoDBDAO;
    }

    /**
     * @param mongoDBDAO the mongoDBDAO to set
     */
    public void setMongoDBDAO(MongoDBDAO mongoDBDAO) {
        this.mongoDBDAO = mongoDBDAO;
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrder(String collectionName, Map<String, String> conditions, DBObject orderBy) {
        return mongoDBDAO.findByExampleLikeAnyWhereWithOrder(collectionName, conditions, orderBy);
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromPattern(String collectionName, Map<String, Pattern> m_pattern) {
        return mongoDBDAO.findByExampleLikeAnyWhereFromPattern(collectionName, m_pattern);
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName, Map<String, Pattern> m_pattern, DBObject orderby) {
        return mongoDBDAO.findByExampleLikeAnyWhereWithOrderFromPattern(collectionName, m_pattern, orderby);
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName, Map<String, Pattern> m_pattern, String operator, DBObject orderby) {
        return mongoDBDAO.findByExampleLikeAnyWhereWithOrderFromPattern(collectionName, m_pattern, operator, orderby);
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, DBObject orderby) {
        return mongoDBDAO.findByExampleLikeAnyWhereFromHead(collectionName, conditions, OROperator, orderby);
    }

    @Override
    public List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, int firstResult, int maxResult) {
        return mongoDBDAO.findByExampleLikeAnyWhereFromHead(collectionName, conditions, OROperator, firstResult, maxResult);
    }

    @Override
    public DBObject findOneByExampleCaseInsesitive(String collectionName, Map<String, String> conditions) {
        return mongoDBDAO.findOneByExampleCaseInsesitive(collectionName, conditions);
    }

    @Override
    public JSONArray bulkSaveFromCopy(String collectionName, BasicDBList entity_array) {
        return mongoDBDAO.bulkSaveFromCopy(collectionName, entity_array);
    }

    @Override
    public JSONArray bulkSetIDProperty(String collectionName, DBObject[] entity_array) {
        return mongoDBDAO.bulkSetIDProperty(collectionName, entity_array);
    }

}
