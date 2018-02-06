<%-- 
    Document   : index
    Created on : Apr 28, 2015, 10:42:29 AM
    Author     : hanyan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" buffer="1000kb"%>
<% String basePath = request.getContextPath(); %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>RERUM Registration</title>
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
        
        #test_api, #login, #refresh_status{
            display: none;
        }
        
        .sep{
            margin-bottom: 35px;
            border: 1px solid #979A9E;
            padding: 14px 12px;
        }
        
        .statusHeader {
            font-size: 14pt;
            margin-bottom: 5px;
        }
        
        .statusNotice{
            margin-bottom: 10px;
        }
        
    </style>
    <body>
        <h1 onclick="window.location='http://rerum.io'" target="_blank" class="navbar-brand"><i class="fa fa-cubes"></i> rerum</h1>
        <div class='container col-xs-10 col-sm-10 col-md-10 col-lg-10' id="intro">
            We are so glad you are interested in using Saint Louis University's public object store, RERUM!  Our store likes to screen calls, so if you would like it
            to answer you must share your server IP address with it. Supply any label you would like stored with your IP address (because RERUM doesn't want to think
            of everyone as just a number).  
        </div>
        <div class='sep container col-xs-10 col-sm-10 col-md-10 col-lg-10' name="block" >
            <div class="statusHeader"> Server Registration </div>
<!--            <label for="server_name"> Your Name </label> <input type="text" class="form-control" id="name" name="name" maxlength="50" />
            <br>
            <label for="serverIP">Your email:</label> <input class="form-control" type="text" id="email" name="email" maxlength="75" />
            <br>
            <label for="serverIP">Website:</label> <input class="form-control" type="text" id="website" name="website" maxlength="75" />
            <br>
            <span id="msg" style="display: block;margin-bottom:10px; margin-top: 5px;"></span>-->
            <p> If you have never been here before and need to register to use RERUM, we will point you to Auth0 to do so.  Please click the link below to get started. </p>
            <input class='btn btn-primary btn-large' type="button" id="regsiter" value=" Register With RERUM At Auth0" /> 

        </div>
        <br><br><br>
        
        <div class='sep container col-xs-10 col-sm-10 col-md-10 col-lg-10' name="block">
            <p>
                If you are registered, your account must be authorized through auth0.  Auth0 will supply you with an authorization code that RERUM
                can use to verify who you are and your right to use the API.
            </p>
            <div class="statusNotice">
                <div class="statusHeader"> Auth0 Status </div>
                <span  class="status" id="authorizationStatus">UNKNOWN</span>
            </div>
            <input class='btn btn-primary btn-large' type="button" id="check_status" value=" Check my Authorization Status With Auth0" />
            <input class='btn btn-primary btn-large' type="button" id="refresh_status" value=" Authorize With Auth0 " />
            <input class='btn btn-primary btn-large' type="button" id="login" value=" Authorize with Auth0 " />
        </div>
        <br><br><br>
        
        <div class='sep container col-xs-10 col-sm-10 col-md-10 col-lg-10' name="block">
            <p> If you would like to check your ability to use RERUM, first check you are authorized with auth0 then click the button below. </p>
            <div class="statusNotice">
                <div class="statusHeader"> RERUM status </div>
                <span class="status" id="rerumStatus">UNKNOWN</span>
            </div>
            <input class='btn btn-primary btn-large' type="button" onclick="testAPI()" id="test_api" value=" Check Access To RERUM API " />
        </div>
    </body>
    <script type="text/javascript">
        /*
         * The process we are mimicking here is https://auth0.com/docs/api-auth/grant/authorization-code
         * Further info here https://auth0.com/docs/api-auth/tutorials/authorization-code-grant
         * Further info here https://auth0.com/docs/api/authentication#authorization-code-grant
         * 

         */
        var access_token = "";
        var auth_code = "";
        var error_code = "";
        var responseJSON = {};
        var myURL = document.location.href;

        if(myURL.indexOf("code=") > -1){ //Status check says authorized.
           auth_code = getURLVariable("code");
           if(auth_code !== ""){
               getAccessToken(auth_code);
               $("#authorizationStatus").html("AUTHORIZED");
           }
           else{ //Bad authorization code
               $("#authorizationStatus").html("UNAUTHORIZED");
           }
           $("#check_status").hide();
       }
       else if (myURL.indexOf("error=") > -1){ //Status check saus unauthorized
           error_code = getURLVariable("error");  
           $("#authorizationStatus").html("UNAUTHORIZED");
           if(error_code == "login_required"){ //Could not get authorization code.  Do a loud login
               $("#login").show();
           }
           else if (error_code == "consent_required"){ //Request the authorization code to get access token
               $("#refresh_status").show();
           }
           else if (error_code == "interaction_required"){
              alert("AHHHHHHHHHHHH");
           }
           $("#check_status").hide();
       }
       else{ //User came to web page, we don't know much about them.  
           
       }

        
        $("#regsiter").click(function(){
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email openid",
                "response_type":"code",
                "client_id":"jwkd5YE0YA5tFxGxaLW9ALPxAyA6Qw1v",
                "redirect_uri":"http://devstore.rerum.io",
                "state":"statious123"           
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            console.log(getURL);
            document.location.href = getURL;
        });
        
        $("#refresh_status").click(function(){
           $("#check_status").click(); 
        });
        
        $("#check_status").click(function(){
        //Be silent about it here because if the user is logged in we don't need to go to the prompt.
        //https://auth0.com/docs/api-auth/tutorials/silent-authentication
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email openid",
                "response_type":"code",
                "client_id":"jwkd5YE0YA5tFxGxaLW9ALPxAyA6Qw1v",
                "redirect_uri":"http://devstore.rerum.io",
                "state":"statious123",
                "prompt" : "none"
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            console.log(getURL);
            document.location.href = getURL;
            
//            xhr.open("GET", getURL, true); 
//            xhr.setRequestHeader("Content-type", "application/json"); 
//            xhr.send();
        });
        
        $("#login").click(function(){
            //This will send them off to the hosted login page https://auth0.com/docs/hosted-pages/login
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email openid",
                "response_type":"code",
                "client_id":"jwkd5YE0YA5tFxGxaLW9ALPxAyA6Qw1v",
                "redirect_uri":"http://devstore.rerum.io",
                "state":"statious123"           
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            document.location.href = getURL;
//            xhr.open("GET", getURL, true); 
//            xhr.setRequestHeader("Content-type", "application/json"); 
//            xhr.send();

        });
        
        function getAccessToken(authorization_code){
            //Failing because of CORS https://auth0.com/docs/cross-origin-authentication
            var params = { 
                "grant_type" : "authorization_code", 
                "client_id":"jwkd5YE0YA5tFxGxaLW9ALPxAyA6Qw1v",
                "client_secret":"Ndy34oet4AtZy7tzBKbhjmU6TVGAW9LdeufUgNXCu9yt1SM4L8uXJzFAfkNBzWRH",
                "code":authorization_code,
                "redirect_uri":"http://devstore.rerum.io"
            }; 
            var postURL = "https://cubap.auth0.com/oauth/token"; 
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (this.readyState === XMLHttpRequest.DONE) {
                    if(this.response !== ""){
                        responseJSON = JSON.parse(this.response); //Outputs a DOMString by default
                        access_token = responseJSON.access_token;
                        console.log("GOT ACCESS TOKEN!");
                        $("#test_api").show();
                        $("#login").hide(); 
                        $("#refresh_status").hide();
                        $("#check_status").hide();
                    }
                    else{
                        $("#rerumStatus").html("Auth0 Rejected Token Request.  Try to refresh your status and if you still have trouble, contact us at RERUM.");
                        $("#test_api ").hide();
                        $("#login").hide(); 
                        $("#refresh_status").show();
                    }
                }
            };
            xhr.open("POST", postURL, true); 
            xhr.setRequestHeader("Content-type", "application/json"); 
            xhr.send(JSON.stringify(params));
        }
        
        function getURLVariable(variable){
            var query = window.location.search.substring(1);
            var vars = query.split("&");
            for (var i=0;i<vars.length;i++) {
                    var pair = vars[i].split("=");
                    if(pair[0] == variable){return pair[1];}
            }
            return false;
        }
        
        function testAPI(){
            var params = { 
                "@type" : "oa:Annotation", 
                "motivation" : "sc:painting", 
                "label" : "Access Test", 
                "resource" : { 
                    "@type" : "cnt:ContentAsText", 
                    "cnt:chars" : "This is a test!" 
                }, 
                "on" : "" 
            }; 
            var postURL = "http://devstore.rerum.io/rerumserver/v1/create.action"; 
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (this.readyState === XMLHttpRequest.DONE) {
                   if(this.status === 201){
                       $("#rerumStatus").html("AUTHORIZED");
                       $("#test_api").show();
                   }
                   else{
                       $("#rerumStatus").html("UNAUTHORIZED.  Try to refresh your status and if you still have trouble, contact us at RERUM.");
                   }
                }
            };
            xhr.open("POST", postURL, true); 
            xhr.setRequestHeader("Content-type", "application/json"); 
            xhr.setRequestHeader("Bearer", access_token); 
            xhr.send(JSON.stringify(params));
        }
        

    </script>
</html>
