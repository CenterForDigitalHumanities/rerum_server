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
public class CompactorService {
    
    /**
     * Default, where the object to compact is provided.
     * @param objectToCompact A JSONObject to compact and return.
     * @return compacted JSONObject
     */
    public JSONObject compact(JSONObject objectToCompact){
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
        Iterator values = objectToCompact.values();
        List<String> valueKeys = Arrays.asList("val","value","@value");
        while(values.hasNext()){
            <T> val = values.next();
            if(val instanceof Object){
                Set<T> valuesOnly = val.keySet().stream()
                    .filter(key->valueKeys.contains(key))
                    .collect(Collectors.toSet());
                if(valuesOnly.hasNext()>0) {
                    val = valuesOnly.next().getValue();
                    // There should only be one thing here, unless there are several
                    // matches against `valueKeys` which is unexpected.
                }
            }
        }
        // changes to `val` should be back by the Object Map, so objectToCompact 
        // ought to be changing.
        return objectToCompact;
    }

    /**
     * Simplify embedded objects to URI, if possible.
     * @param objectToCompact A JSONObject to compress (by removing unwanted keys)
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
        private JSONObject minimizeReferences(JSONObject objectToCompact) {
            Iterator values = objectToCompact.values();
                while(values.hasNext()){
                    <T> val = values.next();
                    if(val instanceof Object){
                        if (val.keySet().contains("@id")) {
                            val = val.get("@id");
                            continue;
                        }
                        
                        if (val.keySet().contains("id")) {
                            val = val.get("id");
                        }   
                    }
                }
                return objectToCompact;
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
