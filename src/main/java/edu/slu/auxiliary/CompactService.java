package edu.slu.auxiliary;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;

/**
 * Transform an object from the database into a version of the object without 
 * metadata keys, with values reduced as much as possible, and with all full 
 * or partially dereferenced reduced to their URI.
 */
public class CompactService {
    
    public static Object Compactor;

    /**
     * Default, where the object to compact is provided.
     * @param objectToCompact A JSONObject to compact and return.
     * @return compacted JSONObject
     */
    public JSONObject Compactor(JSONObject objectToCompact){
        JSONObject toReturn = stripProperties(objectToCompact);
        toReturn = minimizeReferences(toReturn);
        toReturn = simplifyValues(toReturn);
        return toReturn;
    }
    
    /**
     * Simplify values to literal, if possible.
     * @param objectToCompact A JSONObject to compress (by removing unwanted keys)
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
    private JSONObject simplifyValues(JSONObject objectToCompact) {
        JSONObject orig = JSONObject.fromObject(objectToCompact);
        return orig;
    }

    /**
     * Simplify embedded objects to URI, if possible.
     * @param objectToCompact A JSONObject to compress (by removing unwanted keys)
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
        private JSONObject minimizeReferences(JSONObject objectToCompact) {
            JSONObject orig = JSONObject.fromObject(objectToCompact);
            return orig;
    }

    /**
     * Strip JSONObject properties that should not be returned.
     * @param objectToCompact A JSONObject to compress (by removing unwanted keys)
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
    private JSONObject stripProperties(JSONObject objectToCompact){
        JSONObject orig = JSONObject.fromObject(objectToCompact);
        List<String> removeableProperties = Arrays.asList("__rerum","_id");
        Iterator keys = objectToCompact.keys();
        try{
            while(keys.hasNext()){
                String key = (String)keys.next();
                if(removeableProperties.contains(key)){
                    objectToCompact.remove(key);
                }
            }
            return objectToCompact;
        }
        catch(Exception e){
            System.out.println("Could not compact object.  See error below.");
            System.out.println(e);
            return orig;
        }
    }
}
