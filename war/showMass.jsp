<%@page import="hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax" %>
<%@page import="java.util.List" %>
<%@page import="hr.pbf.digestdb.web.WebListener" %>
<%@page import="hr.pbf.digestdb.uniprot.UniprotLevelDbFinder" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         errorPage="pages/errorPage.jsp"
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<c:set var="headTitle" value="Search result"/>
<c:set var="pageName" value="Page title"/>
<%@ include file="pages/_header.jsp" %>
<body>
<%@ include file="pages/_navbar.jsp" %>
<div class="container main">
    <%
        UniprotLevelDbFinder finder = WebListener.getFinder();
        float mass = Float.parseFloat(request.getParameter("mass"));
        List<PeptideAccTax> result = finder.searchMass(mass);
        request.setAttribute("result", result);
    %>

    <display:table name="result" class="table" pagesize="2">
    <display:setProperty name="paging.banner.no_items_found">
        <div class="pagination">No {0} found.</div>
    </display:setProperty>
    <display:setProperty name="paging.banner.one_item_found">
        <div class="pagination">One {0} found.</div>
    </display:setProperty>
    <display:setProperty name="paging.banner.all_items_found">
        <div class="pagination">{0} {1} found, displaying all {2}.</div>
    </display:setProperty>
    <display:setProperty name="paging.banner.some_items_found">
        <div class="pagination">{0} {1} found, displaying {2} to {3}.</div>
    </display:setProperty>
    <display:setProperty name="paging.banner.full">
        <div class="pagination">
            <ul>
                <li><a href="{1}">First</a></li>
                <li><a href="{2}">&laquo;</a></li>
                <li>{0}</li>
                <li><a href="{3}">&raquo;</a></li>
                <li><a href="{4}">Last</a></li>
            </ul>
        </div>
    </display:setProperty>
    <display:setProperty name="paging.banner.first">
        <div class="pagination">
            <ul>
                <li class="disabled"><span>First</span></li>
                <li class="disabled"><span>&laquo;</span></li>
                <li>{0}</li>
                <li><a href="{3}">&raquo;</a></li>
                <li><a href="{4}">Last</a></li>
            </ul>
        </div>
    </display:setProperty>
    <display:setProperty name="paging.banner.last">
        <div class="pagination">
            <ul>
                <li><a href="{1}">First</a></li>
                <li><a href="{2}">&laquo;</a></li>
                <li>{0}</li>
                <li class="disabled"><span>&raquo;</span></li>
                <li class="disabled"><span>Last</span></li>
            </ul>
        </div>
    </display:setProperty>
    <display:setProperty name="paging.banner.page.separator">
    </li>
    <li>
        </display:setProperty>
        <display:setProperty name="paging.banner.page.selected">
        <span class="active">{0}</span>
        </display:setProperty>
        <display:setProperty name="paging.banner.onepage">
        <div class="pagination">{0}</div>
        </display:setProperty>
            <%--<display:column property="log_id" title="ID" sortable="true"/>--%>
            <%--<display:column property="log_ts" title="Timestamp" sortable="true"/>--%>
            <%--<display:column property="log_type" title="Type" sortable="true"--%>
                            <%--class="inputSuccess"/>--%>
            <%--<display:column property="source_thread_name" title="Thread"--%>
                            <%--sortable="true"/>--%>
            <%--<display:column property="source" title="Source" sortable="true"/>--%>
            <%--<display:column property="log_msg" title="Message" sortable="true"/>--%>
        </display:table>

</div>
</body>
</html>