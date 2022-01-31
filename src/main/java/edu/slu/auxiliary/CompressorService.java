/*
 * Gimme some JSON.  I'll give you back less JSON.
 * @author bhaberbe

 * @notes
 * In my mind, someone is calling this hoping RERUM knows how to compress it.
 * If they were to go as far as providing the keys to be removed, they would just do it themselves.
 * Instead, you provide the Compressor keys to keep, and let it go to work on your JSON objects.  
 * We can have default keys we know we must keep.
 * We can have default keys we know must bee removed.
 * Anything else has to be provided.  Keys to keep can be provided.  
 */
package edu.slu.auxiliary;

import java.util.Arrays;
import java.util.Iterator;
import net.sf.json.JSONObject;

public class CompressorService {
    //Bring in Global or Utility functionality from the auxiliary package.
    private Controller aux_controller = new Controller();
    
    //These are keys that we want to make sure stay with the JSONObject.
    private final String[] primitiveKeys = 
        {
            //"__rerum", //Hmm do we want some kind of flag for whether or not to keep this?
            "@id",
            "id", 
            "type", 
            "@type", 
            "label", 
            "name", 
            "summary", 
            "description", 
            "@context"
        };
    
    //These are keys that will be removed 100% of the time.  Maybe just __rerum.
    private final String[] alwaysRemove = {
        "__rerum"
    };
    
    /**
     * Default Initializer.  Probably just to use CompressorService.compress();
     */
    public CompressorService(){
        
    }
    
    /**
     * Initialize default compressing, where the JSONObject to compress is provided.
     * @param toCompress A JSONObject to compress and return.
     * @return The Compressed JSONObject
     */
    public JSONObject Compressor(JSONObject toCompress){
        return compress(toCompress, primitiveKeys);
    }
    
    /**
     * Initialize the compressor, where the keys to keep are provided as a String[] array
     * @param toCompress A JSONObject to compress and return.
     * @param keysToKeep The keys of the JSONObject that should remain after compression.
     * @return The Compressed JSONObject
     */
    public JSONObject Compressor(JSONObject toCompress, String[] keysToKeep){
        if(keysToKeep.length == 0){
            return compress(toCompress, primitiveKeys);
        }
        else{
            return compress(toCompress, keysToKeep);
        }
    }
    
    /**
     * Remove keys from a JSONObject that should not be kept because of compression.
     * This will remove every key from the provided JSONObject that is not in the provided array of keys to keep.
     * @param toCompress A JSONObject to compress (by removing unwanted keys)
     * @param keysToKeep A String[] Array of keys that should not be removed.
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
    private JSONObject compress(JSONObject toCompress, String[] keysToKeep){
        Arrays.asList(keysToKeep).addAll(Arrays.asList(primitiveKeys)); //This way an implementor cannot override the keys we think need to stay.
        Iterator keys = toCompress.keys();
        JSONObject orig = JSONObject.fromObject(toCompress);
        try{
            while(keys.hasNext()){
                String key = (String)keys.next();
                if(!Arrays.asList(keysToKeep).contains(key)){
                    toCompress.remove(key);
                }
            }
            return toCompress;
        }
        catch(Exception e){
            System.out.println("Could not compress object.  See error below.");
            System.out.println(e);
            return orig;
        }
    }
    
    private JSONObject polish(JSONObject o){
        JSONObject orig = JSONObject.fromObject(o);
        try{
            for(int i=0; i<alwaysRemove.length; i++){
                String key = alwaysRemove[i];
                o.remove(key);
            }
            return o;
        }
        catch(Exception e){
            System.out.println("Could not polish object.  See error below.");
            System.out.println(e);
            return orig;
        }
    }
}
