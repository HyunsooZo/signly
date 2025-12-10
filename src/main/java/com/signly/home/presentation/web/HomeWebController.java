package com.signly.home.presentation.web;

import com.signly.common.security.CurrentUserProvider;
import com.signly.common.security.SecurityUser;
import com.signly.common.web.BaseWebController;
import com.signly.home.application.DashboardService;
import com.signly.home.application.dto.DashboardResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 홈/대시보드 웹 컨트롤러
 * SRP: 뷰 렌더링과 요청/응답 처리만 담당
 */
@Controller
@RequiredArgsConstructor
public class HomeWebController extends BaseWebController {

    private static final Logger logger = LoggerFactory.getLogger(HomeWebController.class);
    private final DashboardService dashboardService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/")
    public String root() {
        return "home/landing";
    }

    @GetMapping("/home")
    public String home(
            @RequestHeader(value = "X-User-Id", defaultValue = "01ARZ3NDEKTSV4RRFFQ69G5FAV") String userId,
            @AuthenticationPrincipal SecurityUser securityUser,
            HttpServletRequest request,
            Model model) {
        try {
            String resolvedUserId = currentUserProvider.resolveUserId(securityUser, request, userId, true);

            // DashboardService를 통해 모든 대시보드 데이터 조회
            DashboardResponse dashboardData = dashboardService.getDashboardData(resolvedUserId);

            model.addAttribute("pageTitle", "대시보드");
            model.addAttribute("recentTemplates", dashboardData.recentTemplates());
            model.addAttribute("recentContracts", dashboardData.recentContracts());
            model.addAttribute("templateStats", dashboardData.templateStats());
            model.addAttribute("contractStats", dashboardData.contractStats());

            // 현재 사용자 정보 추가
            if (securityUser != null) {
                model.addAttribute("currentUserName", securityUser.getName());
                model.addAttribute("currentUserEmail", securityUser.getEmail());
                model.addAttribute("currentUserId", securityUser.getUserId());
                model.addAttribute("currentUserCompany", securityUser.getCompanyName());
                model.addAttribute("currentUserBusinessPhone", securityUser.getBusinessPhone());
                model.addAttribute("currentUserBusinessAddress", securityUser.getBusinessAddress());
            }

            return "home/dashboard";

        } catch (Exception e) {
            logger.error("대시보드 로드 중 오류 발생", e);
            model.addAttribute("errorMessage", "대시보드를 불러오는 중 오류가 발생했습니다.");
            return "home/dashboard";
        }
    }
}
