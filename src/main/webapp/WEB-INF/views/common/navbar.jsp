<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" href="/home">
            <i class="bi bi-file-earmark-text me-2"></i>Signly
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <div class="navbar-nav ms-auto">
                <a class="nav-link ${currentPage == 'home' ? 'active' : ''}" href="/home">대시보드</a>
                <a class="nav-link ${currentPage == 'templates' ? 'active' : ''}" href="/templates">템플릿</a>
                <a class="nav-link ${currentPage == 'contracts' ? 'active' : ''}" href="/contracts">계약서</a>
                <a class="nav-link ${currentPage == 'signature' ? 'active' : ''}" href="/profile/signature">서명 관리</a>
                <a class="nav-link" href="/logout">로그아웃</a>
            </div>
        </div>
    </div>
</nav>
