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
        <title>III-F Store</title>
        <script language="javascript" type="text/javascript" src="<%=basePath%>/js/jquery-1.9.1.min.js" charset="utf-8"></script>
    </head>
    <body>
        <div name="block" >
            <h3>Server Registration</h3>
            Server name: <input type="text" id="server_name" name="serverName" style="display: block; margin-bottom:10px; margin-top: 5px;" maxlength="50" />
            Server IP: <input type="text" id="server_ip" name="serverIP" style="display: block; margin-bottom:5px; margin-top: 5px;" maxlength="20" />
            <span id="msg" style="display: block;margin-bottom:10px; margin-top: 5px;"></span>
            <input type="button" id="submit_server_reg" value=" Submit " />
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
        });
    </script>
</html>
