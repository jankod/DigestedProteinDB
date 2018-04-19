<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="pages/_header.jsp" %>
<body>
<%@ include file="pages/_navbar.jsp" %>
<div class="container main">

    <div class="card" style="width: 28rem;">
        <div class="card-body">
            <form class="" novalidate method="get" action="data.jsp" id="formSearch">
                <div class="row">
                    <div class="col">
                        <input type="text" class="form-control" id="massFrom" name="massFrom" placeholder="Mass from"
                               value="2000">
                    </div>
                    <div class="col">
                        <input type="text" class="form-control" id="massTo" name="massTo" placeholder="Mass to"
                               value="2000.3">
                    </div>
                </div>
                <div class="form-group">
                    <button type="submit" class="btn btn-primary">Search</button>
                </div>
            </form>
        </div>
    </div>

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
            var table =  $('#table').DataTable({
                "dom": '<"top"iflp<"clear">>rt<"bottom"pil<"clear">>',
                "processing": true,
                "serverSide": true,
                "ordering": false,
                "searching": false,
                "ajax": {
                    url: "/search",
                    // url: window.location.href,
                    type: "POST",
                    //  contentType: 'application/json;',
                    //contentType: 'application/json; charset=utf-8',

                    //   dataSrc: '',
                    data: function (d) {
                        d.massFrom = $("#massFrom").val()+"";
                        d.massTo = $("#massTo").val()+"";
                        //  return d;
                        var data = JSON.stringify(d);
                       // console.log("data", data);
                        return data;
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

            $("#formSearch").submit(function (event) {
               table.ajax.reload();
                event.preventDefault();
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
