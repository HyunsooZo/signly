<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<header class="header">
    <div class="header-container">
        <!-- 로고 -->
        <div class="header-logo">
            <a href="<c:url value='/' />" class="logo-link">
                <img src="<c:url value='/images/logo.png' />" alt="Signly" class="logo-img">
                <span class="logo-text">Signly</span>
            </a>
        </div>

        <!-- 네비게이션 메뉴 -->
        <nav class="header-nav">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <!-- 로그인 상태 메뉴 -->
                    <ul class="nav-list">
                        <li class="nav-item">
                            <a href="<c:url value='/contracts' />" class="nav-link">
                                <i class="icon-contracts"></i>
                                계약서 관리
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="<c:url value='/templates' />" class="nav-link">
                                <i class="icon-templates"></i>
                                템플릿 관리
                            </a>
                        </li>
                        <li class="nav-item dropdown">
                            <a href="#" class="nav-link dropdown-toggle" id="userDropdown">
                                <i class="icon-user"></i>
                                <c:out value="${sessionScope.user.name}" />
                                <i class="icon-chevron-down"></i>
                            </a>
                            <ul class="dropdown-menu" id="userDropdownMenu">
                                <li>
                                    <a href="<c:url value='/profile' />" class="dropdown-item">
                                        <i class="icon-profile"></i>
                                        프로필 관리
                                    </a>
                                </li>
                                <li>
                                    <a href="<c:url value='/profile/signature' />" class="dropdown-item">
                                        <i class="bi bi-pencil"></i>
                                        서명 관리
                                    </a>
                                </li>
                                <li>
                                    <a href="<c:url value='/settings' />" class="dropdown-item">
                                        <i class="icon-settings"></i>
                                        설정
                                    </a>
                                </li>
                                <li class="dropdown-divider"></li>
                                <li>
                                    <a href="<c:url value='/logout' />" class="dropdown-item">
                                        <i class="icon-logout"></i>
                                        로그아웃
                                    </a>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </c:when>
                <c:otherwise>
                    <!-- 비로그인 상태 메뉴 -->
                    <ul class="nav-list">
                        <li class="nav-item">
                            <a href="<c:url value='/about' />" class="nav-link">서비스 소개</a>
                        </li>
                        <li class="nav-item">
                            <a href="<c:url value='/contracts/search' />" class="nav-link">계약서 조회</a>
                        </li>
                        <li class="nav-item">
                            <a href="<c:url value='/login' />" class="nav-link">로그인</a>
                        </li>
                        <li class="nav-item">
                            <a href="<c:url value='/register' />" class="btn btn-primary">회원가입</a>
                        </li>
                    </ul>
                </c:otherwise>
            </c:choose>
        </nav>

        <!-- 모바일 메뉴 토글 -->
        <button class="mobile-menu-toggle" id="mobileMenuToggle">
            <span class="hamburger-line"></span>
            <span class="hamburger-line"></span>
            <span class="hamburger-line"></span>
        </button>
    </div>
</header>
