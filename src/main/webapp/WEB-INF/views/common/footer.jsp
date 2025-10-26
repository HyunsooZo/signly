<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/alerts.js"></script>
<c:if test="${not empty additionalJs}">
    <c:forEach var="jsFile" items="${additionalJs}">
        <script src="${jsFile}"></script>
    </c:forEach>
</c:if>
