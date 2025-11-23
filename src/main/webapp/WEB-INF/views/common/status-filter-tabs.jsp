<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    상태 필터 Pills 탭 공통 컴포넌트
    
    파라미터:
    - filterType: "template" 또는 "contract"
    - currentStatus: 현재 선택된 상태 값
--%>
<c:set var="filterType" value="${param.filterType}"/>
<c:set var="currentStatus" value="${param.currentStatus}"/>

<ul class="nav nav-pills mb-4" role="tablist">
    <li class="nav-item" role="presentation">
        <a class="nav-link ${empty currentStatus ? 'active' : ''}"
           href="?status=">
            전체
        </a>
    </li>

    <c:choose>
        <c:when test="${filterType == 'template'}">
            <li class="nav-item" role="presentation">
                <a class="nav-link ${currentStatus == 'ACTIVE' ? 'active' : ''}"
                   href="?status=ACTIVE">
                    활성
                </a>
            </li>
            <li class="nav-item" role="presentation">
                <a class="nav-link ${currentStatus == 'ARCHIVED' ? 'active' : ''}"
                   href="?status=ARCHIVED">
                    보류
                </a>
            </li>
        </c:when>

        <c:when test="${filterType == 'contract'}">
            <li class="nav-item" role="presentation">
                <a class="nav-link ${currentStatus == 'DRAFT' ? 'active' : ''}"
                   href="?status=DRAFT">
                    초안
                </a>
            </li>
            <li class="nav-item" role="presentation">
                <a class="nav-link ${currentStatus == 'PENDING' ? 'active' : ''}"
                   href="?status=PENDING">
                    서명 대기
                </a>
            </li>
            <li class="nav-item" role="presentation">
                <a class="nav-link ${currentStatus == 'SIGNED' ? 'active' : ''}"
                   href="?status=SIGNED">
                    서명 완료
                </a>
            </li>
            <li class="nav-item" role="presentation">
                <a class="nav-link ${currentStatus == 'CANCELLED' ? 'active' : ''}"
                   href="?status=CANCELLED">
                    취소
                </a>
            </li>
            <li class="nav-item" role="presentation">
                <a class="nav-link ${currentStatus == 'EXPIRED' ? 'active' : ''}"
                   href="?status=EXPIRED">
                    만료
                </a>
            </li>
        </c:when>
    </c:choose>
</ul>
