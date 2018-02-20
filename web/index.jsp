<%-- 
    Document   : index
    Created on : Apr 28, 2015, 10:42:29 AM
    Author     : hanyan
--%>

<%@page import="edu.slu.action.ObjectAction"%>
<%@page contentType="text/html" pageEncoding="UTF-8" buffer="1000kb"%>
<% String basePath = request.getContextPath(); %>
<%
    String access_token = "",
    auth_code = "";
    //Only generate a new access token if the user specifically said they wanted a new one.  Otherwise, just track the auth_code which tells us they were authorized at Auth0.
    if (request.getParameter("code") != null && !request.getParameter("code").equals("") &&
        request.getParameter("access") != null && request.getParameter("access").equals("true")) 
    {
        auth_code = request.getParameter("code");
        access_token = ObjectAction.getAccessTokenWithAuth(auth_code);
    }
    else if(request.getParameter("code") != null && !request.getParameter("code").equals("")){
        auth_code = request.getParameter("code");
    }
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
        
        #test_api, #login, #request_token{
            display: none;
        }
        
        .panel-body{
            color: initial;
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
            <p>
                Interacting with RERUM requires server-to-server communication, so we suggest the registrant be the application developer.  
                You may want to 
                <a target="_blank" href="http://centerfordigitalhumanities.github.io/rerum/web/#/future" class="linkOut">learn more about the concepts around RERUM</a> 
                before reading the API.
            </p>
            <p>
                If you are here for the first time and think you want to use RERUM, please 
                <a target="_blank" href="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md" class="linkOut">read the API</a> first.
            </p>
            
            <p>
                If you like what you read in <a target="_blank" href="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md" class="linkOut">our API documentation</a> 
                and want to begin using RERUM please register by clicking below.  If you already registered, this will create a new access token and overwrite your old one.
            </p>
            <p>Be prepared to be routed to Auth0 (don't know why?
                <a target="_blank" href="https://github.com/CenterForDigitalHumanities/rerum_server/blob/master/API.md" class="linkOut">Read the API</a>).
            </p>
            </div>
            <div class="panel-footer">
            <input class='btn btn-primary btn-large' type="button" id="register" value="Register With RERUM At Auth0" /> 
            </div>
        </div>

        <div class='panel panel-info' name="block" >
            <div class="panel-body">
            <p>
                If you believe are already registered and want to check on your status with RERUM, follow the prompts below.  You may be routed to Auth0 so we can verify who you are.  
            </p>
            <div>
                <span> Auth0 Status </span> 
                <kbd class="status" id="authorizationStatus">UNKNOWN</kbd>
            </div>
            </div>
            <div class="panel-footer">
            <input class='btn btn-primary btn-large' type="button" id="check_status" value="Check my Authorization Status With Auth0" />
            <input class='btn btn-primary btn-large' type="button" id="login" value="Refresh Authorization With Auth0/Login" />
            <input class='btn btn-primary btn-large' type="button" id="request_token" value="Get A New Access Token" />
            </div>
        </div>
        
        <div class='panel panel-info' name="block" >
            <div class="panel-body">
            <p> 
                If you would like to check your ability to use RERUM, first check your status with Auth0 to reveal the button.  If you are known by Auth0, 
                please enter the access token you were provided when you registered for RERUM or requested a new token then click 'Check Access To RERUM API'. 
                If you do not know your access token, you can request a new one after you check your authorization status with Auth0.
                (Note: Your old token may no longer work in certain situations if you re register or request a new token, even if RERUM accepts it here).  
            </p>
            <textarea class="form-control" id="a_t" placeholder="Enter your access token here to check your access to RERUM."></textarea>
            </div>
            <div class="panel-footer">
            <div>
                <span> RERUM status </span> 
                <kbd class="status" id="rerumStatus">UNKNOWN</kbd>
            </div>
                <input class='btn btn-primary btn-large' type="button" onclick="testAPI()" id="test_api" value="Check Access To RERUM API " />
            </div>
        </div>
        
    </body>
    <script type="text/javascript">
        /*
         * The process we are mimicking here is https://auth0.com/docs/api-auth/grant/authorization-code
         * Further info here https://auth0.com/docs/api-auth/tutorials/authorization-code-grant
         * Further info here https://auth0.com/docs/api/authentication#authorization-code-grant
         * 
         */
        var accessSwitch = false;
        var access_token = "";
        var auth_code = "";
        var error_code = "";
        var responseJSON = {};
        var myURL = document.location.href;


        if(myURL.indexOf("code=") > -1){ //User is logged in and consented to use RERUM.  They have an authorization code
           auth_code = getURLVariable("code");
           accessSwitch = getURLVariable("access");
           if(auth_code !== ""){
               if(accessSwitch == "true"){
                    access_token = "<% out.write(access_token); %>";
                    if(access_token.indexOf("Auth0") > -1){
                        //Then it was one of our errors from the back end.
                        $("#authorizationStatus").html("There was an issue getting an access token from auth 0.  Review the message below. \n\
                            <br> message="+access_token);
                    }
                    else{
                        $("#authorizationStatus").html("Thanks for choosing RERUM!  A new token was created for you.  Keep this token in a safe place, you will need it for our API. \n\
                            You can test that your access token will work with RERUM by clicking the 'Test API' button below.  <br> token="+access_token);
                        $("#test_api").show();
                    }
               }
               else{
                    $("#authorizationStatus").html("AUTHORIZED: auth code="+auth_code)+".<br> You cannot ask for your current access token, but you can generate a new one by\n\
                    requesting one below.  If you have not yet registered with RERUM at Auth0, you will need to do that to get your first access token. ";
                    $("#request_token").show();
                    $("#test_api").show();
               }
           }
           else{ //Weird
               $("#authorizationStatus").html("UNAUTHORIZED");
           }
           $("#check_status").hide();
       }
       else if (myURL.indexOf("error=") > -1){ //Status check says unauthorized
           error_code = getURLVariable("error");  
           
           if(error_code == "login_required"){ //Could not get authorization code.  Do a loud login
                $("#authorizationStatus").html("You must login with Auth0 for this check.");
                $("#login").show();
           }
           else if (error_code == "consent_required"){ //User has not consented and has never gotten an access token.  Request the authorization code to get access token
                $("#authorizationStatus").html("You have never consented to use the API, so you do not have an access token.  Get one to test access to RERUM.");
                $("#request_token").show();
           }
           else if (error_code == "interaction_required"){
              $("#authorizationStatus").html("There are strange happenings afoot in the void of the web...");
           }
           $("#check_status").hide();
       }
       else{ //User came to web page, we don't know much about them.  
           
       }

        $("#register").click(function(){
        //Register means sign up at auth0 and authorize to get an auth code.  Then use that auth code to generate a token, so access=true to generate an access token.
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email openid",
                "response_type":"code",
                "client_id":"62Jsa9MxHuqhRbO20gTHs9KpKr7Ue7sl",
                "redirect_uri":"http://devstore.rerum.io?access=true",
                "state":"statious123"           
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            console.log(getURL);
            document.location.href = getURL;
        });
        
         $("#request_token").click(function(){
        //This means they want to authorize and get a new access token, so access is true.
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email openid",
                "response_type":"code",
                "client_id":"62Jsa9MxHuqhRbO20gTHs9KpKr7Ue7sl",
                "redirect_uri":"http://devstore.rerum.io?access=true",
                "state":"statious123",
                "prompt" : "none"
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            console.log(getURL);
            document.location.href = getURL;

        });
        
        $("#check_status").click(function(){
        //This means they just want to see if they are authorized (AKA get an auth code) but DO NOT want to generate a new access token. 
           var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email openid",
                "response_type":"code",
                "client_id":"62Jsa9MxHuqhRbO20gTHs9KpKr7Ue7sl",
                "redirect_uri":"http://devstore.rerum.io?access=false",
                "state":"statious123",
                "prompt" : "none"
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            console.log(getURL);
            document.location.href = getURL;
        });
        
       
        
        $("#login").click(function(){
            //This means silent auth failed and they could not get a code because they werent logged in.  Send them off to login and get an auth code, but DO NOT
            //generate a new access token.  
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email openid",
                "response_type":"code",
                "client_id":"62Jsa9MxHuqhRbO20gTHs9KpKr7Ue7sl",
                "redirect_uri":"http://devstore.rerum.io?access=false",
                "state":"statious123"
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
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
        
        function testAPI(){
            var userProvidedToken = $("#a_t").val();
            if(userProvidedToken !== ""){
                $("#a_t").css("border", "none");
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
                var postURL = "http://devstore.rerum.io/v1/api/create.action"; 
                var xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function() {
                    if (this.readyState === XMLHttpRequest.DONE) {
                       if(this.status === 201){
                           $("#rerumStatus").html("AUTHORIZED");
                           $("#test_api").show();
                       }
                       else{
                           $("#rerumStatus").html("UNAUTHORIZED.  Try to refresh your status and if you still have trouble, <a class='linkOut' href=''>contact us at RERUM</a>.");
                           $("#a_t").css("border", "2px solid red");
                       }
                    }
                };
                xhr.open("POST", postURL, true); 
                xhr.setRequestHeader("Content-type", "application/json"); 
                xhr.setRequestHeader("Bearer", userProvidedToken); 
                xhr.send(JSON.stringify(params));
            }
            else{
                $("#a_t").attr("placeholder", "You must supply an access token here!");
                $("#a_t").css("border", "2px solid yellow");
            }
        }

    </script>
</html>
