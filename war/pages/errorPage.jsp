<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		isErrorPage="true"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<html>
<head>
<title>ServerError</title>
</head>
<body>
		<h3>ServerError</h3>
		 <h1>Opps...</h1>
      <table width = "100%" border = "1">
         <tr valign = "top">
            <td width = "40%"><b>Error:</b></td>
            <td>${pageContext.exception}</td>
         </tr>
            
         <tr valign = "top">
            <td><b>URI:</b></td>
            <td>${pageContext.errorData.requestURI}</td>
         </tr>
            
         <tr valign = "top">
            <td><b>Status code:</b></td>
            <td>${pageContext.errorData.statusCode}</td>
         </tr>
            
         <tr valign = "top">
            <td><b>Stack trace:</b></td>
            <td>
               <c:forEach var = "trace" 
                  items = "${pageContext.exception.stackTrace}">
                  <p>${trace}</p>
               </c:forEach>
            </td>
         </tr>
      </table>

</body>
</html>
