<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="navbar navbar-expand-lg navbar-dark">
    <div class="container">
        <a class="navbar-brand" href="/home">
            Signly
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <div class="navbar-nav ms-auto">
                <a class="nav-link ${param.currentPage == 'home' ? 'active' : ''}" href="/home">대시보드</a>
                <a class="nav-link ${param.currentPage == 'templates' ? 'active' : ''}" href="/templates">템플릿</a>
                <a class="nav-link ${param.currentPage == 'contracts' ? 'active' : ''}" href="/contracts">계약서</a>
                <a class="nav-link ${param.currentPage == 'signature' ? 'active' : ''}" href="/profile/signature">서명
                    관리</a>
                <a class="nav-link" href="/logout">로그아웃</a>
            </div>
        </div>
    </div>
</nav>
