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
    public static final String COLLECTION_ANNOTATION = "annotation";
    public static final String COLLECTION_ACCEPTEDSERVER = "acceptedServer";
    public static final String COLLECTION_USER = "user";
    public static final String COLLECTION_PROJECT_USER_PROFILE = "projectUserProfile";
    public static final String COLLECTION_AGENT = "agent";
    
    public static final String AGENT_DEPENDENCY_TYPE_USER = "user";
    public static final String AGENT_DEPENDENCY_TYPE_STRING = "string";
    
    //number of annotation versions
    public static final int NUMBER_OF_ANNO_VERSION = 10;
    
    //get by objectID url
    //public static final String GET_BY_OBJECTid_URL = "http://store.rerum.io/rerumserver/anno/getAnnotationByObjectID";
    
    //return result message
    public static final String DUPLICATED = "duplicated";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String GOOD = "good";
    public static final String OK = "ok";
}

