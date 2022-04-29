<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         errorPage="pages/errorPage.jsp"
%>
<%@ page import="hr.pbf.digestdb.uniprot.UniprotLevelDbFinder" %>
<%@ page import="hr.pbf.digestdb.web.WebListener" %>
<%@ page import="hr.pbf.digestdb.uniprot.UniprotModel" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>data 2</title>
    <%@ include file="pages/_header.jsp" %>
</head>
<body>
<%
    if (request.getAttribute("page") == null) {
        request.setAttribute("page", 1);
    }
    UniprotLevelDbFinder finder = WebListener.getFinder();
    List<UniprotModel.PeptideAccTaxNames> result = finder.findData2(request);
    request.setAttribute("result", result);
%>
<script>
    $(document).ready(function () {

        $("#formSearch").submit(function (event) {
            //  event.preventDefault();

        });

    });
</script>
<div class="card" style="width: 28rem;">
    <div class="card-body">
        <form class="" novalidate method="get" action="data2.jsp" id="formSearch">
            <div class="row">
                <div class="col">
                    <input type="text" class="form-control" id="massFrom" name="massFrom"
                           placeholder="Mass from" value="2000">
                </div>
                <div class="col">
                    <input type="text" class="form-control" id="massTo" name="massTo"
                           placeholder="Mass to" value="2000.3">
                </div>
            </div>
            <input type="hidden" name="page" value="<%=request.getAttribute("page")%>">
            <div class="form-group">
                <button type="submit" class="btn btn-primary">Search</button>
            </div>
        </form>
    </div>
</div>
<div>
</div>
<table id="table" class="table table-striped table-bordered" style="width: 100%">
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
    <c:forEach varStatus="loop" items="${result}" var="p">
        <tr>
            <td>${loop.index}</td>
            <td> ${p.peptide}</td>
            <td>${p.acc}</td>
            <td>${p.protName}</td>
            <td>${p.tax}- ${p.taxName}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
