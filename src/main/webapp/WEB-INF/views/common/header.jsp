<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:if test="${not empty param.pageTitle}">${param.pageTitle} - </c:if>Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <c:if test="${not empty param.additionalCss}">
        <link href="${param.additionalCss}" rel="stylesheet">
    </c:if>
    <c:if test="${not empty param.additionalCss2}">
        <link href="${param.additionalCss2}" rel="stylesheet">
    </c:if>
    <c:if test="${not empty param.additionalCss3}">
        <link href="${param.additionalCss3}" rel="stylesheet">
    </c:if>
</head>
