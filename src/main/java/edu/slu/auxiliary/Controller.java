/**
 *
 * @author bhaberbe
 * @author cubap
 */
package edu.slu.auxiliary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.json.JSONObject;

public class Controller {
    private final JSONObject orig_document;
    private JSONObject serviced_document;
    private final Map<String,String[]> params;
    
    public Controller(JSONObject json, Map<String,String[]> queryParameters){
        orig_document = JSONObject.fromObject(json.toString());
        serviced_document = JSONObject.fromObject(json.toString());
        params = queryParameters;
        processFlags();
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

    public void processFlags() {
        Set<String> validFlags = params.keySet()
        .stream()
        .distinct()
        .filter(flagsList::contains)
        .collect(Collectors.toSet());
        if (validFlags.size()>0)
        {
            try { // maybe everyone gets a try later
                if (validFlags.contains("compress")) {
                    System.out.println("this.compress()");
                    this.compress();
                }
                else if (validFlags.contains("compact")) {
                    System.out.println("this.compact()");
                    this.compact();
                }
                else if (validFlags.contains("expand")){
                    System.out.println("this.expand()");
                    this.expand();
                }
            } catch (Exception e) {
                Logger.getLogger(Controller.class.getName()).info("Could not process flag.\n" + e.getMessage());
            }
        }
    }
    
    /**
     * Remove keys from a JSONObject that should not be kept because of compression.
     * This will remove every key from the provided JSONObject that is not in the array of default primitive keys.
     * @return compressed JSON or the original if there was an error.
     */
    public void compress(){
        Set<String> keysToKeep = new HashSet<>(Arrays.asList(params.get("keysToKeep[]"))); 
        if(keysToKeep == null) {
            serviced_document = new CompressorService().compress(serviced_document);
        }
        else{
            serviced_document = new CompressorService().compress(serviced_document, keysToKeep);
        }
    }
    
    /**
     * Reduce key values within a JSONObject.
     * @return compacted JSON or the original if there was an error.
     */
    public JSONObject compact(){
        return new CompactorService().compact(serviced_document);
    }
    
    /**
     * Expand internal references in JSONObject.
     * @return expanded JSON or the original if there was an error.
     */
    public JSONObject expand(){
        return new ExpanderService().expand(serviced_document);
    }
    
    public JSONObject getServicedDocument(){
        return serviced_document;
    }
    
    public JSONObject getOriginalDocument(){
        return serviced_document;
    }
}
