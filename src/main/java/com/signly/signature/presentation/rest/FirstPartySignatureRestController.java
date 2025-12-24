package com.signly.signature.presentation.rest;

import com.signly.common.exception.ValidationException;
import com.signly.common.security.UserPrincipal;
import com.signly.signature.application.FirstPartySignatureService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/first-party-signature")
@RequiredArgsConstructor
public class FirstPartySignatureRestController {

    private static final Logger logger = LoggerFactory.getLogger(FirstPartySignatureRestController.class);

    private final FirstPartySignatureService firstPartySignatureService;

    @GetMapping("/me")
    public ResponseEntity<MySignatureResponse> getMySignature(
            @AuthenticationPrincipal UserPrincipal securityUser,
            HttpServletRequest request
    ) {
        try {
            String userId = null;
            if (securityUser != null) {
                userId = securityUser.getUserId();
            }
            if (userId == null && request != null) {
                Object userIdAttr = request.getAttribute("userId");
                if (userIdAttr instanceof String id && !id.isBlank()) {
                    userId = id;
                }
            }

            if (userId == null || userId.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            var metadata = firstPartySignatureService.getSignature(userId);
            String dataUrl = firstPartySignatureService.getSignatureDataUrl(userId);
            MySignatureResponse response = new MySignatureResponse(dataUrl, metadata.updatedAt());
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            logger.debug("등록된 갑 서명이 없어 204 반환: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    public record MySignatureResponse(String dataUrl, java.time.LocalDateTime updatedAt) {}
}
