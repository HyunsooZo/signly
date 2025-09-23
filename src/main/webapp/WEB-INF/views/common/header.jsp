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

<style>
.header {
    background-color: white;
    border-bottom: 1px solid var(--gray-200);
    height: var(--header-height);
    position: sticky;
    top: 0;
    z-index: 1000;
}

.header-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
}

.header-logo {
    display: flex;
    align-items: center;
}

.logo-link {
    display: flex;
    align-items: center;
    text-decoration: none;
    color: var(--gray-900);
}

.logo-img {
    height: 32px;
    width: auto;
    margin-right: 0.5rem;
}

.logo-text {
    font-size: var(--font-size-xl);
    font-weight: 700;
    color: var(--primary-color);
}

.header-nav .nav-list {
    display: flex;
    align-items: center;
    list-style: none;
    margin: 0;
    padding: 0;
    gap: 1rem;
}

.nav-item {
    position: relative;
}

.nav-link {
    display: flex;
    align-items: center;
    padding: 0.5rem 1rem;
    color: var(--gray-600);
    text-decoration: none;
    border-radius: var(--border-radius);
    transition: all 0.2s ease;
    gap: 0.5rem;
}

.nav-link:hover {
    color: var(--primary-color);
    background-color: var(--gray-50);
}

.nav-link i {
    font-size: var(--font-size-base);
}

.dropdown-toggle::after {
    margin-left: 0.5rem;
}

.dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    background-color: white;
    border: 1px solid var(--gray-200);
    border-radius: var(--border-radius);
    box-shadow: var(--box-shadow-lg);
    min-width: 200px;
    padding: 0.5rem 0;
    display: none;
    z-index: 1000;
}

.dropdown-menu.show {
    display: block;
}

.dropdown-item {
    display: flex;
    align-items: center;
    padding: 0.5rem 1rem;
    color: var(--gray-700);
    text-decoration: none;
    gap: 0.5rem;
}

.dropdown-item:hover {
    background-color: var(--gray-50);
    color: var(--primary-color);
}

.dropdown-divider {
    height: 1px;
    background-color: var(--gray-200);
    margin: 0.5rem 0;
}

.mobile-menu-toggle {
    display: none;
    background: none;
    border: none;
    cursor: pointer;
    flex-direction: column;
    gap: 3px;
    padding: 0.5rem;
}

.hamburger-line {
    width: 20px;
    height: 2px;
    background-color: var(--gray-600);
    transition: all 0.3s ease;
}

/* 모바일 반응형 */
@media (max-width: 768px) {
    .header-nav {
        display: none;
    }

    .mobile-menu-toggle {
        display: flex;
    }

    .header-nav.mobile-open {
        display: block;
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        background-color: white;
        border-bottom: 1px solid var(--gray-200);
        padding: 1rem;
    }

    .header-nav.mobile-open .nav-list {
        flex-direction: column;
        align-items: stretch;
        gap: 0.5rem;
    }

    .nav-link {
        padding: 0.75rem;
        border-radius: var(--border-radius);
    }

    .dropdown-menu {
        position: static;
        box-shadow: none;
        border: none;
        padding-left: 1rem;
    }
}

/* 아이콘 스타일 (임시 - 실제로는 아이콘 폰트나 SVG 사용) */
.icon-contracts::before { content: "📋"; }
.icon-templates::before { content: "📝"; }
.icon-user::before { content: "👤"; }
.icon-profile::before { content: "👤"; }
.icon-settings::before { content: "⚙️"; }
.icon-logout::before { content: "🚪"; }
.icon-chevron-down::before { content: "▼"; }
</style>