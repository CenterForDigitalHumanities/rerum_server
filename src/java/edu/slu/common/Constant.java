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
    public static final String DATABASE_CONNECTION = "mongodb://some-user:SomePassword@some-server:27017/DBNAME?w=majority&authMechanism=PICK-ONE";
    
    //Mongo Database Name
    public static final String DATABASE_NAME = "annotationStore"; // NOTE this changes between dev and prod
    
    //Database Collection Name
    public static final String COLLECTION_ANNOTATION = "alpha"; //db.alpha.doStuff()
    
    //Legacy Collection Names
    public static final String COLLECTION_ACCEPTEDSERVER = "acceptedServer"; //db.acceptedServer.doStuff()
    public static final String COLLECTION_V0 = "annotation"; // db.annotation.doStuff()
    
    //RERUM URL and endpoint paterns
    public static final String RERUM_BASE="http://test-store.rerum.io";
    public static final String RERUM_PREFIX="http://test-store.rerum.io/prd02-img01/";
    public static final String RERUM_ID_PREFIX="http://test-store.rerum.io/prd02-img01/id/";
    public static final String RERUM_AGENT_ClAIM="http://devstore.rerum.io/v1/agent";
    
    //RERUM API Linked Data context
    public static final String RERUM_CONTEXT="http://test-store.rerum.io/prd02-img01/context.json";
    
    //The location of the public API documents.  This is necessary for JSON-LD context purposes.
    public static final String RERUM_API_DOC="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md#__rerum";
    
    //return result message
    public static final String DUPLICATED = "duplicated";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String GOOD = "good";
    public static final String OK = "ok";
}

