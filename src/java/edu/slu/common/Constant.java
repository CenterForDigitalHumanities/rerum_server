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
    public static final String RERUM_API_VERSION="1.0.0";
    
    //Mongo Connection String
    public static final String DATABASE_CONNECTION = "mongodb://rerum-dev:69Trombones@f-vl-cdh-img-01:27017/annotationStoreDev?w=majority&authMechanism=SCRAM-SHA-256";
    
    //Mongo Database Name
    public static final String DATABASE_NAME = "annotationStoreDev"; // NOTE this changes between dev and prod.  Check the connection string.
    
    //Database Collection Name
    public static final String COLLECTION_ANNOTATION = "alpha"; //db.alpha.doStuff()
    
    //Legacy Collection Names
    public static final String COLLECTION_ACCEPTEDSERVER = "acceptedServer"; //db.acceptedServer.doStuff()
    public static final String COLLECTION_V0 = "annotation"; // db.annotation.doStuff()
    
    //RERUM URL and endpoint paterns
    public static final String RERUM_BASE="http://devstore.rerum.io";
    public static final String RERUM_PREFIX="http://devstore.rerum.io/v1/";
    public static final String RERUM_ID_PREFIX="http://devstore.rerum.io/v1/id/";
    public static final String RERUM_AGENT_ClAIM="http://devstore.rerum.io/v1/agent";
    
    //RERUM API Linked Data context
    public static final String RERUM_CONTEXT="http://devstore.rerum.io/v1/context.json";
    
    //The location of the public API documents.  This is necessary for JSON-LD context purposes.
    public static final String RERUM_API_DOC="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md#__rerum";
    
    //return result message
    public static final String DUPLICATED = "duplicated";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String GOOD = "good";
    public static final String OK = "ok";
}

