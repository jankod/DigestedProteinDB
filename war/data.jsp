<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="pages/_header.jsp" %>
<body>
<%@ include file="pages/_navbar.jsp" %>
<div class="container main">


    <table id="table" class="table table-striped table-bordered" style="width:100%">
        <thead>
        <tr>
            <th>#</th>
            <th>Mass</th>
            <th>Peptide</th>
            <th>Protein</th>
            <th>Taxonomy</th>
        </tr>
        </thead>
    </table>
    <script>
        $(document).ready(function () {
            $('#table').DataTable({
                "processing": true,
                "serverSide": true,
                "ordering": false,
                "searching": false,
                "ajax": {
                    url: "/search",
                    type: "POST",
                  //  contentType: 'application/json;',
                    //contentType: 'application/json; charset=utf-8',

                    //   dataSrc: '',
                    data: function (d) {
                      //  return d;
                      return  JSON.stringify(d);
                    }

                    // }
                },
                "columns": [
                    {"data": "num"},
                    {"data": "mass"},
                    {"data": "peptide"},
                    {"data": "protein"},
                    {"data": "taxonomy"}
                ],
                order: [[0, 'asc']],
                lengthMenu: [20, 50, 100]
            });
        });
        function planify(data) {
            for (var i = 0; i < data.columns.length; i++) {
                column = data.columns[i];
                column.searchRegex = column.search.regex;
                column.searchValue = column.search.value;
                delete(column.search);
            }
        }
    </script>


</div>

</body>
</html>
