<%@page import="hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax" %>
<%@page import="java.util.List" %>
<%@page import="hr.pbf.digestdb.web.WebListener" %>
<%@page import="hr.pbf.digestdb.uniprot.UniprotLevelDbFinder" %>
<%@ page import="hr.pbf.digestdb.uniprot.UniprotModel" %>
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
        List<UniprotModel.PeptideAccTaxNames> result = finder.searchMass(mass);
        request.setAttribute("result", result);
    %>
    <p> Peptides with mass: ${param.mass} Da</p>
</div>

<table id="table" class="table table-striped table-bordered" style="width:100%">
    <thead>
    <tr>
        <th>#</th>
        <th>Peptide</th>
        <th>Accession</th>
        <th>Prot name</th>
        <th>Tax id</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach varStatus="loop" var="p" items="${result}">
        <tr>
            <td>${loop.index+1}</td>
            <td>${p.peptide}</td>
            <td>${p.acc}</td>
            <td>${p.protName}</td>
            <td>${p.tax} - ${p.taxName}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
    $(document).ready(function () {
        $('#table').DataTable({});
    });

</script>
</body>
</html>