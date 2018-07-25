<%-- 
    Document   : index
    Created on : Apr 28, 2015, 10:42:29 AM
    Author     : hanyan
--%>

<%@page import="java.util.ResourceBundle"%>
<%@page import="edu.slu.action.ObjectAction"%>
<%@page contentType="text/html" pageEncoding="UTF-8" buffer="1000kb"%>
<% 
    String basePath = request.getContextPath(); 
    String access_token = "";
    String auth_code = "";
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>RERUM Authorization Portal</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css">
       <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.2.2/jquery.min.js"></script>
       <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    </head>
    <style>
        #msg{
            color: green;
            font-weight: bold;
        }
        
        #intro{
            color: #979A9E;
            font-size: 12pt;
        }
        
        body{
            font-family: 'Open Sans', sans-serif;
            color: #979A9E;
            background-color: #2F353E;
        }
        
        input[type="text"]{
            background-color: #ccc;
            color: black;
            font-weight: bold;
            font-family: serif;
            font-size: 14pt;
        }
        
        h1{
            cursor: pointer;
            font-weight: 300;
            font-family: 'Raleway', sans-serif;
            margin-bottom:10px;
        }
        
        .navbar-brand {
            float: none;
            font-size: 2rem;
            line-height: 1.5;
            margin-bottom: 20px;
        }
        
        #login{
            display: none;
        }
        
        .panel-body{
            color: initial;
        }
        
        .panel{
            word-break: break-word;
        }
        
        .handHoldy{
            
        }
        
        .status_header{
            color: gray;
        }
        
        #a_t{
            height: 170px;
            margin-bottom: 8px;
        }
        
        #a_t, #r_t_4_a_t, #new_refresh_token {
            margin-bottom: 8px;
        }
        
        #code_for_refresh_token{
            margin-bottom: -13px;
        }

        
    </style>
    <body class="container">
        <h1 onclick="window.location='http://rerum.io'" target="_blank" class="navbar-brand"><i class="fa fa-cubes"></i> rerum</h1>
        <div class='' id="intro">
            <p>
                We are so glad you are interested in using Saint Louis University's public object store, RERUM! Want to know what RERUM is all about?
            </p>
            <ol type="1" id='rerumPrinciples'>
                <li><strong>As RESTful as is reasonable—</strong>accept and respond to a broad range of requests without losing the map</li>
                <li><strong>As compliant as is practical—</strong>take advantage of standards and harmonize conflicts</li>
                <li><strong>Save an object, retrieve an object—</strong>store metadata in private (__rerum) property, rather than wrap all data transactions</li>
                <li><strong>Trust the application, not the user—</strong>avoid multiple login and authentication requirements and honor open data attributions</li>
                <li><strong>Open and Free—</strong>expose all contributions immediately without charge to write or read</li>
                <li><strong>Attributed and Versioned—</strong>always include asserted ownership and transaction metadata so consumers can evaluate trustworthiness and relevance</li>
            </ol>
        </div>
        <div class='panel panel-info' name="block" >
            <div class="panel-heading"> <span class="panel-title">Application Registration</span> </div>
            <div class="panel-body">
            <p class="handHoldy">
                Interacting with RERUM requires server-to-server communication, so we suggest the registrant be the application developer.  
                You may want to 
                <a target="_blank" href="http://centerfordigitalhumanities.github.io/rerum/web/#/future" class="linkOut">learn more about the concepts around RERUM</a> 
                before reading the API.
            </p>
            <p class="handHoldy">
                If you are here for the first time and think you want to use RERUM, please 
                <a target="_blank" href="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md" class="linkOut">read the API</a> first.
            </p>
            
            <p class="handHoldy">
                If you like what you read in <a target="_blank" href="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md" class="linkOut">our API documentation</a> 
                and want to begin using RERUM as a back stack service please register by clicking below. 
                Be prepared to be routed to Auth0 (don't know why?
                <a target="_blank" href="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md" class="linkOut">Read the API</a>).
            </p>
            <p class="handHoldy">
                After registering, you will be returned to this page with an Auth0 Authorization code.  Use that code at the bottom of this page to get a refresh token 
                and an access token so you can use the API.  You may notice the page has already populated known information for you.  
            </p>
            </div>
            <div class="panel-footer">
            <input class='btn btn-primary btn-large' type="button" id="register" value="Register With RERUM At Auth0" /> 
            </div>
        </div>

        <div class='panel panel-info' name="block" >
            <div class="panel-heading"> <span class="panel-title">Auth0 Authorization Status</span> </div>
            <div class="panel-body">
            <p class="handHoldy">
                If you believe you are already registered and want to check on your status, follow the prompts below.  You will be routed to Auth0 so we can verify who you are.  
            </p>
            <div>
                <span class="status_header"> Auth0 Status </span> 
                <kbd class="rerumStatus" id="authorizationStatus">UNKNOWN</kbd>
            </div>
            </div>
            <div class="panel-footer">
            <input class='btn btn-primary btn-large' type="button" id="check_status" value="Check my Authorization Status With Auth0" />
            <!--<input class='btn btn-primary btn-large' type="button" id="login" value="Login To Get Auth Status" />-->
            </div>
        </div>
        
        <div class='panel panel-info' name="block" >
            <div class="panel-heading"> <span class="panel-title">Test RERUM API Access</span> </div>
            <div class="panel-body">
            <p class="handHoldy"> 
                Provide your access token below to check if it is still valid.  If so, your access to RERUM will be authorized.  Otherwise, you will see an "unauthorized" message.
            </p>
            <p class="handHoldy">
                If the token you have is not working, it may be because access tokens expire every 2 hours.  You can use your refresh token to get a new access token.
            </p>
            <textarea class="form-control" id="a_t" placeholder="Your access token goes here."></textarea>
            <div>
                <span class="status_header"> RERUM status </span> 
                <kbd class="rerumStatus" id="rerumStatus" class="">UNKNOWN</kbd>
            </div>
            </div>
            <div class="panel-footer">
                <input class='btn btn-primary btn-large' type="button" onclick="testAPI()" id="test_api" value="Check Access To RERUM API" />
            </div>
        </div>
        
        <div class='panel panel-info' name="block" >
            <div class="panel-heading"> <span class="panel-title">Get A New Access Token</span> </div>
            <div class="panel-body">
                <p class="handHoldy">
                    Your access token to use RERUM expires every 2 hours.  Has it been that long or longer? Provide your refresh token below to get a new access token.
                    If you lost your refresh token, you can get a new one in "Get A New Refresh Token" below.
                </p>
                <textarea class="form-control" placeholder="Your refresh token goes here." id="r_t_4_a_t"></textarea>
                <div>
                    <span class="status_header"> Status </span> 
                    <kbd class="rerumStatus" id="natStatus" >UNKNOWN</kbd>
                </div>
            </div>
            <div class="panel-footer">
                <input class='btn btn-primary btn-large' type="button" id="request_token" value="Get A New Access Token" />
            </div>
        </div>
        
        <div class='panel panel-info' name="block" >
            <div class="panel-heading"> <span class="panel-title">Get A New Refresh Token</span> </div>
            <div class="panel-body">
                <p class="handHoldy">
                    You can supply a valid Auth0 Authorization Code to get a new refresh token.  Use "Check my Authorization Status with Auth0" to get a valid code.    
                </p>
                Enter your code: <textarea class="form-control" placeholder="Your Auth0 Authorization Code goes here" id="code_for_refresh_token"></textarea>
                <br>
                <textarea readonly class="form-control" id="new_refresh_token" placeholder="Your refresh token will appear here."></textarea>
                <div>
                    <span class="status_header"> Status </span> 
                    <kbd class="rerumStatus" id="nrtStatus">UNKNOWN</kbd>
                </div>
            </div>
            <div class="panel-footer">
                <input class='btn btn-primary btn-large' type="button" id="refresh_token" value="Get A New Refresh Token" />
            </div>
        </div>
        
    </body>
    <script type="text/javascript">
        /*
         * The process we are mimicking here is https://auth0.com/docs/api-auth/grant/authorization-code and  https://auth0.com/docs/api-auth/tutorials/client-credentials
         * https://auth0.com/docs/api-auth/tutorials/authorization-code-grant
         * https://auth0.com/docs/api/authentication#authorization-code-grant
         * https://auth0.com/docs/api-auth/grant/implicit
         * https://auth0.com/docs/api-auth/grant/authorization-code
         * https://auth0.com/docs/api-auth/tutorials/client-credentials
         */
        var access_token = "";
        var auth_code = "";
        var error_code = "";
        var responseJSON = {};
        var myURL = document.location.href;
        var R_B = document.location.origin;
        var R_P = document.location.origin+"/v1/";

        if(myURL.indexOf("access_token=") > -1){
            //The user registered or asked for a new token through the Client Credentials Grant flow https://auth0.com/docs/api-auth/tutorials/client-credentials
            //Presumably, they had to come to rerum to do this.  They cannot ask for new access tokens through their servers or front end.
            access_token = getURLHash("access_token");
            $("#test_api").show();
            $("#a_t").val(access_token);
            $("#check_status").hide();
            $("#authorizationStatus").html("Thanks for choosing RERUM!  A new token was created for you.  Keep this token in a safe place, you will need it for our API. \n\
                You can test that your access token will work with RERUM by clicking the 'Test API' button below.  <br> token="+access_token);
        }
        else if(myURL.indexOf("code=") > -1){
           //The user simply checked if they were registered.  If so, they get a code.  If not, they get an error.
           auth_code = getURLVariable("code");
           $("#code_for_refresh_token").val(auth_code);
           if(auth_code !== ""){
                $("#authorizationStatus").html("AUTHORIZED: auth code="+auth_code+".  You are registered with the Auth0 RERUM client.  Get a new refresh token using this code.");
           }
           else{ //Weird
               $("#authorizationStatus").html("UNAUTHORIZED");
           }
           $("#check_status").hide();
       }
       else if (myURL.indexOf("error=") > -1){ 
       //The user registered, checked their status or asked for a new access token and there was a problem. 
           error_code = getURLVariable("error");  
           if(error_code == "login_required"){ //What they are asking for requires authentication against their user inside the RERUM Server Auth0 client.
                $("#authorizationStatus").html("You must login with Auth0 for this check.");
                $("#login").show();
           }
           else if (error_code == "consent_required"){ //The user is logged in with auth0 but has not registered with the Rerum Server Auth0 client.
                $("#authorizationStatus").html("You have never consented to use the API, so you do not have an access token.  Get one to test access to RERUM.");
           }
           else if (error_code == "interaction_required"){
              $("#authorizationStatus").html("There are strange happenings afoot in the void of the web...");
           }
           $("#check_status").hide();
       }
       else{ //User came to web page, we don't know much about them.  
           
       }

        $("#register").click(function(){
        //Register means register with the RERUM Server Auth0 client and get a new code for a refresh token.
        //See https://auth0.com/docs/libraries/custom-signup
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"openid name email offline_access",
                //"scope":"name email openid offline_access",
                "response_type":"code",
                //"response_type":"token",
                "client_id":"62Jsa9MxHuqhRbO20gTHs9KpKr7Ue7sl",
                "redirect_uri":R_B,
                "state":"statious123"           
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            console.log(getURL);
            document.location.href = getURL;
        });
        
        $("#request_token").click(function(){
            var r_t = $("#r_t_4_a_t").val();
            var statusElem = $("#natStatus");
            if(r_t){
                $("#r_t_4_a_t").css("border", "none");
                $("#r_t_4_a_t").val();
                var params={
                    "refresh_token":r_t
                };
                var postURL = R_P+"api/accessToken.action"; 
                var xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function() {
                    if (this.readyState === XMLHttpRequest.DONE) {
                        var resp = this.response;
                        if(typeof resp=="string"){
                            resp = JSON.parse(resp);
                        }
                        if(this.status < 300){
                            $("#a_t").val(resp.access_token);
                            statusElem.html("The refresh token was accepted.");
                        }
                        else{
                            $("#r_t_4_a_t").css("border", "2px solid red");
                            statusElem.html("The refresh token was invalid.  Get a new refresh token or try again.");
                        }
                    }
                };
                xhr.open("POST", postURL, true); 
                xhr.setRequestHeader("Content-type", "application/json"); 
                xhr.send(JSON.stringify(params));
            }
            else{
                $("#r_t_4_a_t").attr("placeholder", "You must supply a refresh token here!");
                $("#r_t_4_a_t").css("border", "2px solid yellow");
            }
        });
        
        $("#refresh_token").click(function(){
            //The user would like to request a new access token using the refresh token.  Send them off to log in. 
            var authCode = $("#code_for_refresh_token").val();
            var statusElem = $("#nrtStatus");
            if(authCode){
                $("#code_for_refresh_token").css("border", "none");
                $("#new_refresh_token").css("border", "none");
                $("#new_refresh_token").val("");
                var params = {
                    "authorization_code":authCode
                };
                var postURL = R_P+"api/refreshToken.action"; 
                var xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function(){
                    if (this.readyState === XMLHttpRequest.DONE) {
                        var resp = this.response;
                        if(typeof resp=="string"){
                            resp = JSON.parse(resp);
                        }
                        if(this.status < 300){
                            $("#new_refresh_token").val(resp.refresh_token); 
                            $("#a_t").val(resp.access_token);
                            statusElem.html("Auth0 accepted the code.");
                        }
                        else{
                            $("#code_for_refresh_token").css("border", "2px solid red");
                            statusElem.html("Auth0 rejected the code.  Check your status to get a new code and try again.");
                        }                      
                    }
                };
                xhr.open("POST", postURL, true); 
                xhr.setRequestHeader("Content-type", "application/json"); 
                xhr.send(JSON.stringify(params));
            }
            else{
                $("#code_for_refresh_token").attr("placeholder", "You must supply a code here!");
                $("#code_for_refresh_token").css("border", "2px solid yellow");
            }
        });
        
        $("#check_status").click(function(){
          //This means they just want to see if they are registered with the RERUM Server Auth0 client (just need an auth code).  If they are no logged in, they will see a prompt to log in.
           var params = {
                "audience":"http://rerum.io/api",
                "scope":"openid name email offline_access",
                "response_type":"code",
                "client_id":"62Jsa9MxHuqhRbO20gTHs9KpKr7Ue7sl",
                "redirect_uri":R_B,
                "state":"statious123"            
            };
            //You can add prompt:none here to use the user stored with the cookie, but this forces login so our rules work better.
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            console.log(getURL);
            document.location.href = getURL;
        });
                
        function getURLVariable(variable){
            var query = window.location.search.substring(1);
            var vars = query.split("&");
            for (var i=0;i<vars.length;i++) {
                    var pair = vars[i].split("=");
                    if(pair[0] == variable){return pair[1];}
            }
            return false;
        }
        
        function getURLHash(variable){
            var query = document.location.hash;
            query = query.substr(1);
            var vars = query.split("&");
            for (var i=0;i<vars.length;i++) {
                    var pair = vars[i].split("=");
                    if(pair[0] == variable){return pair[1];}
            }
            return false;
        }
        
        function testAPI(){
            var userProvidedToken = $("#a_t").val();
            var statusElem = $("#rerumStatus");
            if(userProvidedToken !== ""){
                statusElem.html("WORKING...");
                $("#a_t").css("border", "none");
                var params = { 
                    "@id" : R_P+"id/11111", 
                    "access" : "test_"+Date.now()
                }; 
                var postURL = R_P+"api/update.action"; 
                var xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function() {
                    if (this.readyState === XMLHttpRequest.DONE) {
                       if(this.status === 200){
                           statusElem.html("AUTHORIZED");
                           $("#test_api").show();
                       }
                       else{
                           statusElem.html("UNAUTHORIZED.  Refresh your access token and try again.  If you still have trouble, <a class='linkOut' href=''>contact us at RERUM</a>.");
                           $("#a_t").css("border", "2px solid red");
                       }
                    }
                };
                xhr.open("PUT", postURL, true); 
                xhr.setRequestHeader("Content-type", "application/json"); 
                xhr.setRequestHeader("Authorization", "Bearer "+userProvidedToken); 
                xhr.send(JSON.stringify(params));
            }
            else{
                $("#a_t").attr("placeholder", "You must supply an access token here!");
                $("#a_t").css("border", "2px solid yellow");
            }
        }

    </script>
</html>
