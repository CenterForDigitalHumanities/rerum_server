package edu.slu.auxiliary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.sf.json.JSONObject;

/**
 * Transform an object from the database into a version of the object without
 * metadata keys, with values reduced as much as possible, and with all full
 * or partially dereferenced reduced to their URI.
 */
public class CompactorService {

    /**
     * Default, where the object to compact is provided.
     * 
     * @param objectToCompact A JSONObject to compact and return.
     * @return compacted JSONObject
     */
    public JSONObject compact(JSONObject objectToCompact) {
        JSONObject toReturn = stripProperties(objectToCompact);
        toReturn = minimizeReferences(toReturn);
        toReturn = simplifyValues(toReturn);
        return toReturn;
    }

    /**
     * Simplify values to literal, if possible.
     * 
     * @param objectToCompact A JSONObject to compress (by removing unwanted keys)
     * @return toCompress once the keys are removed, or the original if there was an
     *         error.
     */
    private JSONObject simplifyValues(JSONObject objectToCompact) {
        Iterator<?> keys = objectToCompact.keys();
        List<String> valueKeys = Arrays.asList("val", "value", "@value");
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (objectToCompact.get(key) instanceof Map) {
                Map val = (Map) objectToCompact.get(key);
                val.keySet().stream()
                        .filter(key -> valueKeys.contains(key))
                        .forEach(v -> val = v.getValue());
                // There should only be one thing here, unless there are several
                // matches against `valueKeys` which is unexpected.
            }
        }
        // changes to `val` should be back by the Object Map, so objectToCompact
        // ought to be changing.
        return objectToCompact;
    }

    /**
     * Simplify embedded objects to URI, if possible.
     * 
     * @param objectToCompact A JSONObject to compress (by removing unwanted keys)
     * @return toCompress once the keys are removed, or the original if there was an
     *         error.
     */
    private JSONObject minimizeReferences(JSONObject objectToCompact) {
        Iterator<?> keys = objectToCompact.keys();
        while (keys.hasNext()) { // .values() seems busted here
            String key = (String) keys.next();
            if (objectToCompact.get(key) instanceof Map) {
                Map val = (Map) objectToCompact.get(key);
                if (val.keySet().contains("@id")) {
                    val = (Map) val.get("@id");
                    continue;
                }

                if (val.keySet().contains("id")) {
                    val = (Map) val.get("id");
                }
            }

        }
        return objectToCompact;
    }

    /**
     * Strip JSONObject properties that should not be returned.
     * 
     * @param objectToCompact A JSONObject to compress (by removing unwanted keys)
     * @return toCompress once the keys are removed, or the original if there was an
     *         error.
     */
    private JSONObject stripProperties(JSONObject objectToCompact) {
        JSONObject orig = JSONObject.fromObject(objectToCompact);
        List<String> removeableProperties = Arrays.asList("__rerum", "_id");
        Iterator<?> keys = objectToCompact.keys();
        try {
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (removeableProperties.contains(key)) {
                    objectToCompact.remove(key);
                }
            }
            return objectToCompact;
        } catch (Exception e) {
            System.out.println("Could not compact object.  See error below.");
            System.out.println(e);
            return orig;
        }
    }
}
