<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title><c:out value="${pageTitle != null ? pageTitle : 'Signly'}" /></title>

    <!-- CSS -->
    <link href="<c:url value='/css/common.css' />" rel="stylesheet">
    <c:if test="${not empty additionalCss}">
        <c:forEach items="${additionalCss}" var="css">
            <link href="<c:url value='/css/${css}' />" rel="stylesheet">
        </c:forEach>
    </c:if>

    <!-- 메타 태그 -->
    <meta name="description" content="Signly - 안전하고 간편한 전자계약 서비스">
    <meta name="keywords" content="전자계약, 전자서명, 디지털 계약, e-signature">
    <meta name="author" content="Signly">

    <!-- 파비콘 -->
    <link rel="icon" type="image/png" href="<c:url value='/images/favicon.png' />">
</head>
<body>
    <!-- 헤더 -->
    <jsp:include page="header.jsp" />

    <!-- 메인 컨테이너 -->
    <div class="main-container">
        <!-- 사이드바 (로그인한 사용자만) -->
        <c:if test="${not empty sessionScope.user}">
            <jsp:include page="sidebar.jsp" />
        </c:if>

        <!-- 메인 컨텐츠 -->
        <main class="main-content <c:if test='${not empty sessionScope.user}'>with-sidebar</c:if>">
            <!-- 알림 메시지 -->
            <c:if test="${not empty sessionScope.successMessage}">
                <div class="alert alert-success">
                    <c:out value="${sessionScope.successMessage}" />
                </div>
                <c:remove var="successMessage" scope="session" />
            </c:if>

            <c:if test="${not empty sessionScope.errorMessage}">
                <div class="alert alert-danger">
                    <c:out value="${sessionScope.errorMessage}" />
                </div>
                <c:remove var="errorMessage" scope="session" />
            </c:if>

            <c:if test="${not empty sessionScope.warningMessage}">
                <div class="alert alert-warning">
                    <c:out value="${sessionScope.warningMessage}" />
                </div>
                <c:remove var="warningMessage" scope="session" />
            </c:if>

            <!-- 페이지 컨텐츠 -->
            <div class="container">
                <!-- 여기에 각 페이지의 내용이 들어갑니다 -->
                <!-- 이 부분은 각 JSP 페이지에서 오버라이드됩니다 -->
            </div>
        </main>
    </div>

    <!-- 푸터 -->
    <jsp:include page="footer.jsp" />

    <!-- JavaScript -->
    <script src="<c:url value='/js/common.js' />"></script>
    <c:if test="${not empty additionalJs}">
        <c:forEach items="${additionalJs}" var="js">
            <script src="<c:url value='/js/${js}' />"></script>
        </c:forEach>
    </c:if>

    <!-- 페이지별 추가 스크립트 -->
    <c:if test="${not empty pageScript}">
        <script>
            ${pageScript}
        </script>
    </c:if>
</body>
</html>