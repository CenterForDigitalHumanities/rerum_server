///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package edu.slu.test;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import net.sf.json.JSONObject;
//import org.apache.http.HttpEntity;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//
///**
// *
// * @author hanyan
// */
//public class TestFetchAnnoApache {
//    public static void main(String[] args){
//        try {
//            HttpPost httpPost = new HttpPost("http://165.134.241.141/annotationstore/anno/getAnnotationByProperties");
////            httpPost.setHeader(null, null);
//            List<BasicNameValuePair> nvps = new ArrayList();
//            JSONObject jo = new JSONObject();
//            jo.element("@type", "sc:AnnotationList");
//            jo.element("proj", 4351);
//            nvps.add(new BasicNameValuePair("annotation.content", jo.toString()));
//            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//            CloseableHttpClient httpclient = HttpClients.createDefault();
//            CloseableHttpResponse response = httpclient.execute(httpPost);
//            System.out.println(response.getStatusLine());
//            HttpEntity entity = response.getEntity();
//            InputStream in = entity.getContent();
//            BufferedInputStream bis = new BufferedInputStream(in);
//            BufferedReader br = new BufferedReader(new InputStreamReader(bis));
//            String line = "";
//            while((line = br.readLine()) != null){
//                System.out.println(line);
//            }
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(TestFetchAnnoApache.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(TestFetchAnnoApache.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//}
