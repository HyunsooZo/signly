<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="ko">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Signly - 모두를 위한 전자계약</title>

            <!-- Fonts & Icons -->
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link
                href="https://fonts.googleapis.com/css2?family=Lobster&family=Noto+Sans+KR:wght@400;500;700;800&display=swap"
                rel="stylesheet">
            <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">

            <!-- Custom CSS -->
            <link href="/css/landing.css" rel="stylesheet">
        </head>

        <body>

            <div class="landing-container">
                <!-- Hero Section -->
                <div class="hero-section">
                    <h1 class="hero-title">
                        계약의 새로운 기준,<br>
                        <span class="brand-name">Signly</span>
                    </h1>
                    <p class="hero-subtitle">
                        복잡한 종이 계약은 이제 그만.<br>
                        언제 어디서나 간편하게 서명하고 안전하게 보관하세요.
                    </p>
                    <a href="/home" class="cta-button">
                        무료로 시작하기 <i class="bi bi-arrow-right-short"></i>
                    </a>
                </div>

                <!-- Features Section -->
                <div class="features-grid">
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i class="bi bi-pen"></i>
                        </div>
                        <div class="feature-content">
                            <h3 class="feature-title">간편한 서명</h3>
                            <p class="feature-desc">이메일이나 링크로 계약서를 보내고, 몇 번의 클릭만으로 서명을 완료하세요.</p>
                        </div>
                    </div>
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i class="bi bi-shield-check"></i>
                        </div>
                        <div class="feature-content">
                            <h3 class="feature-title">강력한 보안</h3>
                            <p class="feature-desc">모든 계약 과정은 암호화되며, 법적 효력을 갖는 감사 로그가 기록됩니다.</p>
                        </div>
                    </div>
                    <div class="feature-card">
                        <div class="feature-icon-wrapper">
                            <i class="bi bi-folder2-open"></i>
                        </div>
                        <div class="feature-content">
                            <h3 class="feature-title">스마트한 관리</h3>
                            <p class="feature-desc">완료된 계약서를 클라우드에서 안전하게 보관하고 언제든 쉽게 찾아보세요.</p>
                        </div>
                    </div>
                </div>
            </div>

        </body>

        </html>