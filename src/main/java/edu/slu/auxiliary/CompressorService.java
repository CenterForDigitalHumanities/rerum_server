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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.sf.json.JSONObject;

public class CompressorService{
    
    //These are keys that we want to make sure stay with the JSONObject.
    //Think about things like "target" and "on"...are they special?  Current thinking is no, they are just as viable to be removed as any other property. 
    
    private Set<String> primitiveKeys = new HashSet<>(Arrays.asList(
        "@id",
        "id", 
        "type", 
        "@type", 
        "label", 
        "name", 
        "summary", 
        "description", 
        "@context"
    )); 
        
    
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
     * Remove keys from a JSONObject that should not be kept because of compression.
     * This will remove every key from the provided JSONObject that is not in the array of default primitive keys.
     * @param toCompress A JSONObject to compress (by removing unwanted keys)
     * @param keysToKeep A String[] Array of keys that should not be removed.
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
    public JSONObject compress(JSONObject toCompress){
        Iterator<String> keys = toCompress.keySet().iterator();
        JSONObject orig = JSONObject.fromObject(toCompress.toString());
        ArrayList<String> keysToRemove = new ArrayList<>();
        System.out.println("Compress B");
        try{
            while(keys.hasNext()){
                Object key = keys.next();
                System.out.println("key is "+key);
                if(!primitiveKeys.contains((String)key)){
                    System.out.println("Need to remove key "+key);
                    keysToRemove.add((String)key);
                    //keys.remove(); //This throws UnsupportedOperationException
                    //toCompress.remove(key) throws ConcurrentModificationException
                }
            }
            //Ok so we can learn of the keys above, but have to actually interact with the JSON object below.
            Iterator<String> removal = keysToRemove.iterator();
            while(removal.hasNext()){
                String k = removal.next();
                System.out.println("eliminate "+k);
                if(toCompress.containsKey(k)){
                    System.out.println("JSON has key "+k);
                    toCompress.remove(k);
                }
            }
            return toCompress;
        }
        catch(Exception e){
            System.out.println("Could not compress object B.  See error below.");
            System.out.println(e);
            return orig;
        }
    }
    
    /**
     * Remove keys from a JSONObject that should not be kept because of compression.
     * This will remove every key from the provided JSONObject that is not in the provided array of keys to keep.
     * @param toCompress A JSONObject to compress (by removing unwanted keys)
     * @param keysToKeep A String[] Array of keys that should not be removed.
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
    public JSONObject compress(JSONObject toCompress, Set<String> keysToKeep){
        keysToKeep.addAll(primitiveKeys);
        Iterator<String> keys = toCompress.keySet().iterator();
        JSONObject orig = JSONObject.fromObject(toCompress.toString());
        ArrayList<String> keysToRemove = new ArrayList<>();
        System.out.println("Compress B");
        try{
            while(keys.hasNext()){
                Object key = keys.next();
                System.out.println("key is "+key);
                if(!keysToKeep.contains((String)key)){
                    System.out.println("Need to remove key "+key);
                    keysToRemove.add((String)key);
                    //keys.remove(); //This throws UnsupportedOperationException
                    //toCompress.remove(key) throws ConcurrentModificationException
                }
            }
            //Ok so we can learn of the keys above, but have to actually interact with the JSON object below.
            Iterator<String> removal = keysToRemove.iterator();
            while(removal.hasNext()){
                String k = removal.next();
                System.out.println("eliminate "+k);
                if(toCompress.containsKey(k)){
                    System.out.println("JSON has key "+k);
                    toCompress.remove(k);
                }
            }
            return toCompress;
        }
        catch(Exception e){
            System.out.println("Could not compress object B.  See error below.");
            System.out.println(e);
            return orig;
        }
    }
    
    /**
     * Remove all keys from this JSON object that are a part of the default list of keys to always remove.
     * @param o
     * @return 
     */
    public JSONObject polish(JSONObject toPolish){
        JSONObject orig = JSONObject.fromObject(toPolish);
        try{
            for(int i=0; i<alwaysRemove.length; i++){
                String key = alwaysRemove[i];
                toPolish.remove(key);
            }
            return toPolish;
        }
        catch(Exception e){
            System.out.println("Could not polish object.  See error below.");
            System.out.println(e);
            return orig;
        }
    }
}
