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
<%--<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>--%>
<c:set var="headTitle" value="Search result"/>
<c:set var="pageName" value="Page title"/>
<%@ include file="pages/_header.jsp" %>
<body>
<%@ include file="pages/_navbar.jsp" %>
<div class="container main">
    <%
        UniprotLevelDbFinder finder = WebListener.getFinder();
        float mass = 1000f;
        if(request.getParameter("margin") != null) {
            mass = Float.parseFloat(request.getParameter("mass"));
        }
        float margin = 0.1f;
        if(request.getParameter("margin") != null) {
            margin = Float.parseFloat(request.getParameter("margin"));
        }else{

        }
        UniprotLevelDbFinder.SearchOneMassResult result = finder.searchMassOne(mass, margin);
        request.setAttribute("result", result.getResult());
        request.setAttribute("masses", result.getMassesAround());
        request.setAttribute("mass", mass);
    %>
    <p> Peptides with mass: ${param.mass} Da</p>


    <div class="row">
        <ul class="list-group col-2 ">
            <li class="list-group-item list-group-item-info">Masses [Da]</li>

            <c:forEach items="${masses}" var="m">
                <c:choose>
                    <c:when test="${m.key == mass}">
                        <c:set var="active" value="active"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="active" value=""/>
                    </c:otherwise>
                </c:choose>
                <%--<li class="list-group-item ${active}">--%>
                <a class="list-group-item list-group-item-action ${active}"
                   href="showMass.jsp?mass=${m.key}">${m.key}
                    <span class="badge badge-light  badge-">${m.value}</span>
                </a>

                <%--</li>--%>
            </c:forEach>
        </ul>
        <div class="col-10">
            <form action="<c:url value="showMass.jsp" />" method="get" style=""
                  novalidate class="form-inline">

                <div class="mx-sm-3 mb-2">
                    <input class="form-control" placeholder="Mass Da" name="mass" id="mass" value="${param.mass}">
                    &plusmn; <input class="form-control" style="width: 6em" type="number" step="0.02" max="0.30" min="0" name="margin" value="0.01">

                </div>
                <button type="submit" id="btnSubmit" class="btn btn-primary mb-2">Search</button>
            </form>

            <table id="table" class="table table-striped table-bordered" style="width:100%">
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
                <c:forEach varStatus="loop" var="p" items="${result}">
                    <tr>
                        <td>${loop.index+1}</td>
                        <td>${p.peptide}</td>
                        <td>${p.mass}</td>
                        <td>${p.acc}</td>
                        <td>${p.protName}</td>
                        <td>${p.tax} - ${p.taxName}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
<script>
    $(document).ready(function () {
        // $('#table').DataTable({
           // "dom": '<"top "ip<"clear">>rt<"bottom"pil<"clear">>'
            //"dom": 'lrtip'
            //"dom": '<lf<t>ip>',
            // dom: '<"row btn-group"<"col-sm-12 col-md-6"p><"col-sm-12 col-md-6"l>><"row"rt><"row"ip>',
            // rowGroup: {
            //     dataSrc: 1
            // }

        // });
    });

</script>
</body>
</html>