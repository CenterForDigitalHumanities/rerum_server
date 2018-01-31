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
        
        #test_api, #login, #submit_auth_reg{
            display: none;
        }
        
    </style>
    <body>
        <h1 onclick="window.location='http://rerum.io'" target="_blank" class="navbar-brand"><i class="fa fa-cubes"></i> rerum</h1>
        <div class='container col-xs-10 col-sm-10 col-md-10 col-lg-10' id="intro">
            We are so glad you are interested in using Saint Louis University's public object store, RERUM!  Our store likes to screen calls, so if you would like it
            to answer you must share your server IP address with it. Supply any label you would like stored with your IP address (because RERUM doesn't want to think
            of everyone as just a number).  
        </div>
        <div class='container col-xs-10 col-sm-10 col-md-10 col-lg-10' name="block" >
            <h3>Server Registration</h3>
            <label for="server_name"> Server name: </label> <input type="text" class="form-control" id="server_name" name="serverName" maxlength="50" />
            <br>
            <label for="serverIP">Server IP:</label> <input class="form-control" type="text" id="server_ip" name="serverIP" maxlength="20" />
            <span id="msg" style="display: block;margin-bottom:10px; margin-top: 5px;"></span>
            <!-- <input class='btn btn-primary btn-large' type="button" id="submit_server_reg" value=" Submit " /> -->
            <input class='btn btn-primary btn-large' type="button" id="submit_auth_reg" value=" Authorize Auth0 To Use This App On Your Behalf" />
            <input class='btn btn-primary btn-large' type="button" id="login" value=" Log In To Auth0 " />
            <input class='btn btn-primary btn-large' type="button" id="test_api" value=" Test RERUM API " />
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

        if(myURL.indexOf("code=") > -1){ //User is logged in and consented to use RERUM.  They have an authorization code
           auth_code = getURLVariable("code");
           if(auth_code !== ""){
               getAccessCode(auth_code);
           }
           else{ //Bad authorization code, they need to go get it again. 
               $("#submit_auth_reg").show();
           }
       }
       else if (myURL.indexOf("error=") > -1){ //Silent log in failed
           error_code = getURLVariable("error");  
           if(error_code == "login_required"){ //Could not get authorization code.  Do a loud login
               $("#login").show();
           }
           else if (error_code == "consent_required"){ //Request the authorization code to get access token
               $("#submit_auth_reg").show();
           }
           else if (error_code == "interaction_required"){
              alert("AHHHHHHHHHHHH");
           }
       }
       else{ //User came to web page, we don't know much about them.  Let them try to consent to use this and get an authorization code
           $("#submit_auth_reg").show(); //Let them try to consent, do a silent log int
       }

        
        $("#submit_server_reg").click(function(){
            var reg = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
            if(reg.test($('#server_ip').val())){
                $.post('<%=basePath%>/acceptedServer/saveNewServer.action', 
                {
                    "acceptedServer.name": $('#server_name').val(), 
                    "acceptedServer.ip": $('#server_ip').val()
                }).done(function(data){
                    data = JSON.parse(data);
                    $('#msg').html(data.info);
                });
            }
            else{
                console.log("reg test failed");
           }
        });
        
        $("#submit_auth_reg").click(function(){
        //Be silent about it here because if the user is logged in we don't need to go to the prompt.
        //https://auth0.com/docs/api-auth/tutorials/silent-authentication
            var xhr = new XMLHttpRequest();
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email",
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
            var xhr = new XMLHttpRequest();
            var params = {
                "audience":"http://rerum.io/api",
                "scope":"name email",
                "response_type":"code",
                "client_id":"jwkd5YE0YA5tFxGxaLW9ALPxAyA6Qw1v",
                "redirect_uri":"http://devstore.rerum.io",
                "state":"statious123"           
            };
            var getURL = "https://cubap.auth0.com/authorize?" + $.param(params);
            console.log(getURL);
            document.location.href = getURL;
//            xhr.open("GET", getURL, true); 
//            xhr.setRequestHeader("Content-type", "application/json"); 
//            xhr.send();

        });
        
        function getAccessCode(authorization_code){
            //Failing because of CORS https://auth0.com/docs/cross-origin-authentication
            var params = { 
                "grant_type" : "authorization_code", 
                "client_id":"jwkd5YE0YA5tFxGxaLW9ALPxAyA6Qw1v",
                "client_secret":"",
                "code":authorization_code,
                "redirect_uri":"http://devstore.rerum.io"
            }; 
            var postURL = "https://cubap.auth0.com/oauth/token"; 
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                console.log("request state change to "+this.readyState);
                console.log("request response is");
                console.log(this.response);
                if (this.readyState === XMLHttpRequest.DONE) {
                    if(this.response !== ""){
                        responseJSON = JSON.parse(this.response); //Outputs a DOMString by default
                        access_code = responseJSON.access_code;
                        $("#test_api ").show();
                        $("#login").hide(); 
                        $("#submit_auth_reg").hide();
                    }
                    else{
                        alert("CANNOT GET ACCESS TOKEN");
                        $("#test_api ").hide();
                        $("#login").show(); 
                        $("#submit_auth_reg").hide();
                    }
                }
            };
            console.log("get access code here");
            console.log(postURL);
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
                "label" : "1-5-18 Tester", 
                "resource" : { 
                    "@type" : "cnt:ContentAsText", 
                    "cnt:chars" : "This is a test!" 
                }, 
                "on" : "" 
            }; 
            var postURL = "http://devstore.rerum.io/rerumserver/v1/create.action"; 
            var xhr = new XMLHttpRequest();
            xhr.open("POST", postURL, false); 
            xhr.setRequestHeader("Content-type", "application/json"); 
            xhr.setRequestHeader("Bearer", access_code); 
            xhr.send(JSON.stringify(params));
        }

    </script>
</html>
