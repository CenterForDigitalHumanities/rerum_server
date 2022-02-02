/**
 *
 * @author bhaberbe
 * @author cubap
 */
package edu.slu.auxiliary;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import net.sf.json.JSONObject;

public class Controller {
    private final JSONObject document;
    private final Map<String,String[]> params;
    
    public Controller(JSONObject json, Map<String,String[]> queryParameters){
        document = json;
        params = queryParameters;
    }

    /**
     * Map of API flags from the query string and the resulting method call.
     * Organized in order of operations?
     */
    private final List<String> flagsList = Arrays.asList(
            "expand",
            "compact",
            "recurse",
            "batch",
            "decscribe",
            "compress");

    public JSONObject processFlags() {
        Set<String> validFlags = params.keySet()
        .stream()
        .distinct()
        .filter(flagsList::contains)
        .collect(Collectors.toSet());
        if (validFlags.size()>0)
        {
            JSONObject servicedDocument = JSONObject.fromObject(document.toString());
            try { // maybe everyone gets a try later
                if (validFlags.contains("compress")) {
                    servicedDocument = this.compress();
                }
                else if (validFlags.contains("compact")) {
                    servicedDocument = this.compact();
                }
                else if (validFlags.contains("expand")){
                    servicedDocument = this.expand();
                }
            } catch (Exception e) {
                Logger.getLogger(Controller.class.getName()).info("Could not process flag.\n" + e.getMessage());
            }
            return servicedDocument;
        }
        return document;
    }
    
    /**
     * Remove keys from a JSONObject that should not be kept because of compression.
     * This will remove every key from the provided JSONObject that is not in the array of default primitive keys.
     * @return compressed JSON or the original if there was an error.
     */
    public JSONObject compress(){
        Set<String> keysToKeep = new HashSet<String>(Arrays.asList(params.get("key[]"))); 
        if(keysToKeep == null) {
            return new CompressorService().compress(document);
        }
        return new CompressorService().compress(document, keysToKeep);
    }
    
    /**
     * Reduce key values within a JSONObject.
     * @return compacted JSON or the original if there was an error.
     */
    public JSONObject compact(){
        return new CompactorService().compact(document);
    }
    
    /**
     * Expand internal references in JSONObject.
     * @return expanded JSON or the original if there was an error.
     */
    public JSONObject expand(){
        return new ExpanderService().expand(document);
    }
}
