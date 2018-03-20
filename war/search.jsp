<%@page import="hr.pbf.digestdb.web.WebListener"%>
<%@page import="hr.pbf.digestdb.uniprot.UniprotLevelDbFinder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="pages/errorPage.jsp"
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="headTitle" value="Search" />
<c:set var="pageName" value="Page title" />
<%@ include file="pages/_header.jsp"%>
<body>
		<%@ include file="pages/_navbar.jsp"%>
		<div class="container main">
		
		<div>
		From mass: <%=request.getParameter("fromMass") %> Da<br>
		To mass: <%=request.getParameter("toMass") %> Da
		
		</div>
		
				<div>
						<%
							String errMsg = null;
							try {
								double fromMass = Double.parseDouble(request.getParameter("fromMass"));
								double toMass = Double.parseDouble(request.getParameter("toMass"));

								UniprotLevelDbFinder finder = WebListener.getFinder();
								UniprotLevelDbFinder.SearchResult res = finder.searchIndex(fromMass, toMass);
							
								out.write(res.toString());
								
							} catch (Throwable t) {
								errMsg = t.getMessage();
							}
						%>
						
						<div class="alert alert-dark" role="alert">
						</div>
						
							
				</div>
		</div>
</body>
</html>
