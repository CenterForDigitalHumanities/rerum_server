/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.common;

/**
 *
 * @author hanyan
 */
public class Constant {
    
    //permission of annotation
    public static final int PERMISSION_PRIVATE = 0;
    public static final int PERMISSION_PROJECT = 1;
    public static final int PERMISSION_PUBLIC = 2;
    
    //collection name
    public static final String COLLECTION_ANNOTATION = "alpha"; // for RERUM alpha
    public static final String COLLECTION_ACCEPTEDSERVER = "acceptedServer";
    public static final String COLLECTION_USER = "user";
    public static final String COLLECTION_PROJECT_USER_PROFILE = "projectUserProfile";
    public static final String COLLECTION_AGENT = "agent";
    
    public static final String AGENT_DEPENDENCY_TYPE_USER = "user";
    public static final String AGENT_DEPENDENCY_TYPE_STRING = "string";
    
    public static final String RERUM_BASE="http://18.218.227.205:8080";
    public static final String RERUM_PREFIX="http://18.218.227.205:8080/rerum_server/";
    public static final String RERUM_ID_PREFIX="http://18.218.227.205:8080/rerum_server/id/";
    public static final String RERUM_AGENT_PREFIX="http://18.218.227.205:8080/rerum_server/agent/";
    public static final String RERUM_AGENT_ClAIM="http://18.218.227.205:8080/rerum_server/agent";
    public static final String RERUM_CONTEXT="http://18.218.227.205:8080/rerum_server/context.json";
    public static final String RERUM_API_VERSION="1.0.0";
    //The location of the public API documents.  This is necessary for JSON-LD context purposes.
    public static final String RERUM_API_DOC="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md#__rerum";
    
    //number of annotation versions
    public static final int NUMBER_OF_ANNO_VERSION = 10;
    
    //get by objectID url
    //public static final String GET_BY_OBJECTid_URL = "http://devstore.rerum.io/rerumserver/anno/getAnnotationByObjectID";
    
    //return result message
    public static final String DUPLICATED = "duplicated";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String GOOD = "good";
    public static final String OK = "ok";
}

