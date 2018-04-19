<%@page import="hr.pbf.digestdb.web.WebListener" %>
<%@page import="hr.pbf.digestdb.uniprot.UniprotLevelDbFinder" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
         errorPage="pages/errorPage.jsp"
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="headTitle" value="Search"/>
<c:set var="pageName" value="Page title"/>
<%@ include file="pages/_header.jsp" %>
<body>
<%@ include file="pages/_navbar.jsp" %>
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
                        UniprotLevelDbFinder.IndexResult res = finder.searchIndex(fromMass, toMass);

                        request.setAttribute("result", res.map);
                        // out.write(res.toString());

                    } catch (Throwable t) {
                        errMsg = t.getMessage();
                    }
        %>

        <table id="table" class="table table-striped table-bordered" style="width:100%">
            <thead>
            <tr>
                <th>#</th>
                <th>Mass</th>
                <th>Number of Peptides</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach varStatus="loop" var="entry" items="${result}">
                <tr>
                    <td>${loop.index+1}</td>
                    <td><a href="showMass.jsp?mass=${entry.key}">${entry.key} Da</a></td>
                    <td>${entry.value}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <div class="alert alert-dark" role="alert">
        </div>


    </div>
</div>
<script>
    $(document).ready(function () {
        $('#table').DataTable({
          //  "dom": '<"top"iflp<"clear">>rt<"bottom"pil<"clear">>'
            "dom": 'lrtip'
            //"dom": '<lf<t>ip>'
            //dom: '<"row btn-group"<"col-sm-12 col-md-6"B><"col-sm-12 col-md-6"l>><"row"rt><"row"ip>',

        });
    });

</script>
</body>
</html>
