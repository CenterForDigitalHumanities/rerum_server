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
public class InsertSort {
    public static List<Integer> seq = new LinkedList();
    
    public static void main(String[] args){
        Random r = new Random();
        for(int i = 0; i < 10; i++){
            seq.add(r.nextInt(20));
        }
    }
}
