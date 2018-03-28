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
    Search one mass

    <div class="card" style="width: 28rem;">
        <div class="card-body">
            <form class="" novalidate method="get" action="data2.jsp" id="formSearch">
                <div class="row">
                    <div class="col">
                        <input type="text" class="form-control" id="mass" name="mass"
                               placeholder="Mass from" value="1234">
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
            <th>Mass</th>
            <th>Accession</th>
            <th>Prot name</th>
            <th>Tax id</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach varStatus="loop" items="${result}" var="p">
            <tr>
                <td>${loop.index}</td>
                <td>${p.peptide}</td>
                <th>${p.mass}</th>
                <td>${p.acc}</td>
                <td>${p.protName}</td>
                <td>${p.tax}- ${p.taxName}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
