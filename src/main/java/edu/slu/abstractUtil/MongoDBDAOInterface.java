/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.abstractUtil;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.sf.json.JSONArray;

/**
 * @author hanyan
 */
public interface MongoDBDAOInterface {
    List<DBObject> findAll(String collectionName, int firstResult, int maxResults);
    List<DBObject> findAll(String collectionName);
    List<DBObject> findAllWithOrder(String collectionName, int firstResult, int maxResults, DBObject orderby);
    List<DBObject> findByExample(String collectionName, DBObject queryEntity);
    List<DBObject> findByExample(String collectionName, DBObject queryEntity, int firstResult, int maxResults);
    List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions);
    List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, DBObject orderby);
    List<DBObject> findByExampleLikeAnyWhere(String collectionName, Map<String, String> conditions);
    List<DBObject> findByExampleLikeAnyWhereFromPattern(String collectionName, Map<String, Pattern> m_pattern);
    List<DBObject> findByExampleLikeAnyWhereWithOrder(String collectionName, Map<String, String> conditions, DBObject orderBy);
    List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName, Map<String, Pattern> m_pattern, DBObject orderby);
    List<DBObject> findByExampleLikeAnyWhereWithOrderFromPattern(String collectionName, Map<String, Pattern> m_pattern, String operator, DBObject orderby);
    List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, int firstResult, int maxResult);
    List<DBObject> findByExampleLikeAnyWhereFromHead(String collectionName, Map<String, String> conditions, String OROperator, int firstResult, int maxResult);
    List<DBObject> findByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject orderBy);
    JSONArray bulkSaveMetadataForm(String collectionName, BasicDBList entity_array);
    JSONArray bulkSaveFromCopy(String collectionName, BasicDBList entity_array);
    JSONArray bulkSetIDProperty(String collectionName, DBObject[] entity_array);
    DBObject findOneByExample(String collectionName, DBObject entity);
    DBObject findOneByExampleWithOrder(String collectionName, DBObject queryEntity, DBObject returnFields, DBObject orderBy);
    DBObject findOneByExampleCaseInsesitive(String collectionName, Map<String, String> conditions);
    void delete(String collectionName, DBObject queryEntity);
    void update(String collectionName, DBObject queryEntity, DBObject targetEntity);
    String save(String collectionName, DBObject targetEntity);
    //String save(String collectionName, BasicDBList targetEntity);
    Long count(String collectionName);
    Long count(String collectionName, DBObject queryEntity);
}
