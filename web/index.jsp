<%-- 
    Document   : index
    Created on : Apr 28, 2015, 10:42:29 AM
    Author     : hanyan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <% String basePath = request.getContextPath(); %>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Annotation Store Registration</title>
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
        
    </style>
    <body>
        <h1 onclick="window.location='http://rerum.io'" target="_blank" class="navbar-brand"><i class="fa fa-cubes"></i> rerum</h1>
        <div class='container col-xs-10 col-sm-10 col-md-10 col-lg-10' id="intro">
            We are so glad you are interested in using Saint Louis University's annotation store, RERUM!  Our annotation store likes to screen calls, so if you would like it
            to answer you must share your server IP address with it.  Supply any label you would like stored with your IP address (because RERUM doesn't want to think of everyone
            as just a number).  
        </div>
        <div class='container col-xs-10 col-sm-10 col-md-10 col-lg-10' name="block" >
            <h3>Server Registration</h3>
            <label for="server_name"> Server name: </label> <input type="text" class="form-control" id="server_name" name="serverName" maxlength="50" />
            <br>
            <label for="serverIP">Server IP:</label> <input class="form-control" type="text" id="server_ip" name="serverIP" maxlength="20" />
            <span id="msg" style="display: block;margin-bottom:10px; margin-top: 5px;"></span>
<!--            <label for="serverIP">Identifier: </label> <input class="form-control" type="text" id="identifier" name="identifier" maxlength="200" />
            <span id="msg" style="display: block;margin-bottom:10px; margin-top: 5px;"></span>-->
            <input class='btn btn-primary btn-large' type="button" id="submit_server_reg" value=" Submit " />
        </div>
    </body>
    <script type="text/javascript">
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
   
    </script>
</html>
