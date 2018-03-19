<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="headTitle" value="Home" />
<%@ include file="pages/_header.jsp"%>
<body>
		<%@ include file="pages/_navbar.jsp"%>
		<div class="container main">
				<form action="<c:url value="search.jsp" />" method="get" style="margin-top: 60px"
						novalidate
				>
						<div class="form-group">
								<label for="fromMass">Mass from: </label> <input class="form-control"
										name="fromMass" id="fromMass"
								>
						</div>
						<div class="form-group">
								<label for="toMass">Mass to: </label> <input class="form-control" name="toMass"
										id="toMass"
								>
						</div>
						<button type="submit" id="btnSubmit" class="btn btn-primary">Submit</button>
				</form>
		</div>
</body>
</html>
