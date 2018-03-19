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
						<%
							try {
								double fromMass = Double.parseDouble(request.getParameter("fromMass"));
								double toMass = Double.parseDouble(request.getParameter("toMass"));

								UniprotLevelDbFinder finder = WebListener.getFinder();
								UniprotLevelDbFinder.SearchResult res = finder.searchIndex(fromMass, toMass);
						%>
						<div>
								Total mass
								<%=res.totalMass%></div>
						<div>
								Total peptides:
								<%=res.totalPeptides%></div>
						<%
							} catch (Throwable t) {

							}
						%>
				</div>
		</div>
</body>
</html>
