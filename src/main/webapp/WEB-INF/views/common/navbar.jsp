<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <c:set var="hideNavLinks" value="${param.hideLinks == 'true'}" />
        <nav class="navbar navbar-expand-lg navbar-dark">
            <div class="container">
                <a class="navbar-brand" href="/home">
                    Signly
                </a>
                <c:if test="${not hideNavLinks}">
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <i class="bi bi-list"></i>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarNav">
                        <div class="navbar-nav ms-auto">
                            <a class="nav-link ${param.currentPage == 'home' ? 'active' : ''}" href="/home">
                                <i class="bi bi-grid-1x2-fill"></i> 대시보드
                            </a>
                            <a class="nav-link ${param.currentPage == 'templates' ? 'active' : ''}" href="/templates">
                                <i class="bi bi-file-earmark-text-fill"></i> 템플릿
                            </a>
                            <a class="nav-link ${param.currentPage == 'contracts' ? 'active' : ''}" href="/contracts">
                                <i class="bi bi-file-earmark-check-fill"></i> 계약서
                            </a>
                            <a class="nav-link ${param.currentPage == 'signature' ? 'active' : ''}"
                                href="/profile/info">
                                <i class="bi bi-person-fill"></i> 나의 정보
                            </a>
                            <a class="nav-link" href="#" onclick="handleLogout(event)">
                                <i class="bi bi-box-arrow-right"></i> 로그아웃
                            </a>
                        </div>
                    </div>
                </c:if>
            </div>
        </nav>