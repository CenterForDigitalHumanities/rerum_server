/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author hanyan
 */
public class LongestSubSeq {
    public static List<Integer> seq = new LinkedList();
    
    public static void main(String[] args){
        Random r = new Random();
        System.out.print("raw: {");
        for(int m = 0; m < 100000; m++){
            int n  = r.nextInt(200000);
            if(m != 99){
                System.out.print(n + ", ");
            }else{
                System.out.print(n);
            }
            seq.add(n);
        }
        System.out.println("}");
        
        List<Integer> final_result = new LinkedList();
        int length = 0;
        for(int n = 0; n < 100000; n++){
            System.out.println("============================================");
            List<Integer> temp_result = new LinkedList();
            temp_result.add(seq.get(n));
            System.out.println("n = " + n);
            for(int j = n+1; j < 100000; j++){
                if(seq.get(j-1) <= seq.get(j)){
                    System.out.println("j = " + j);
                    temp_result.add(seq.get(j));
                }else{
                    System.out.println("length ====== " + length);
                    System.out.println("temp_result size ======== " + temp_result.size());
                    if(length < temp_result.size()){
                        length = temp_result.size();
                        final_result = temp_result;
                    }
                    n = j - 1;
                    break;
                }
            }
        }
        System.out.println("length ======== " + length);
        System.out.println("final array ========== " + final_result);
    }
}
