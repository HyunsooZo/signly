package com.signly.domain.contract.model;

import com.signly.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class SignatureTest {

    @Test
    void 정상적인_정보로_생성할_수_있다() {
        String signerEmail = "test@example.com";
        String signerName = "홍길동";
        String signatureData = "서명데이터";
        String ipAddress = "192.168.1.1";

        Signature signature = Signature.create(signerEmail, signerName, signatureData, ipAddress);

        assertThat(signature.getSignerEmail()).isEqualTo(signerEmail.toLowerCase());
        assertThat(signature.getSignerName()).isEqualTo(signerName);
        assertThat(signature.getSignatureData()).isEqualTo(signatureData);
        assertThat(signature.getIpAddress()).isEqualTo(ipAddress);
        assertThat(signature.getSignedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void 이메일이_소문자로_변환된다() {
        String signerEmail = "TEST@EXAMPLE.COM";

        Signature signature = Signature.create(signerEmail, "홍길동", "서명데이터", "192.168.1.1");

        assertThat(signature.getSignerEmail()).isEqualTo("test@example.com");
    }

    @Test
    void 이름_앞뒤_공백이_제거된다() {
        String signerName = "  홍길동  ";

        Signature signature = Signature.create("test@example.com", signerName, "서명데이터", "192.168.1.1");

        assertThat(signature.getSignerName()).isEqualTo("홍길동");
    }

    @Test
    void null_서명자_이메일로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Signature.create(null, "홍길동", "서명데이터", "192.168.1.1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("서명자 이메일은 필수입니다");
    }

    @Test
    void 빈_서명자_이메일로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Signature.create("", "홍길동", "서명데이터", "192.168.1.1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("서명자 이메일은 필수입니다");
    }

    @Test
    void null_서명자_이름으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Signature.create("test@example.com", null, "서명데이터", "192.168.1.1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("서명자 이름은 필수입니다");
    }

    @Test
    void 빈_서명자_이름으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Signature.create("test@example.com", "", "서명데이터", "192.168.1.1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("서명자 이름은 필수입니다");
    }

    @Test
    void null_서명_데이터로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Signature.create("test@example.com", "홍길동", null, "192.168.1.1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("서명 데이터는 필수입니다");
    }

    @Test
    void 빈_서명_데이터로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Signature.create("test@example.com", "홍길동", "", "192.168.1.1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("서명 데이터는 필수입니다");
    }

    @Test
    void null_IP주소로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Signature.create("test@example.com", "홍길동", "서명데이터", null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("IP 주소는 필수입니다");
    }

    @Test
    void 빈_IP주소로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Signature.create("test@example.com", "홍길동", "서명데이터", ""))
                .isInstanceOf(ValidationException.class)
                .hasMessage("IP 주소는 필수입니다");
    }

    @Test
    void 특정_이메일로_서명했는지_확인할_수_있다() {
        String signerEmail = "test@example.com";
        Signature signature = Signature.create(signerEmail, "홍길동", "서명데이터", "192.168.1.1");

        assertThat(signature.isSignedBy(signerEmail)).isTrue();
        assertThat(signature.isSignedBy("other@example.com")).isFalse();
    }

    @Test
    void 대소문자_구분없이_서명_확인이_가능하다() {
        String signerEmail = "test@example.com";
        Signature signature = Signature.create(signerEmail, "홍길동", "서명데이터", "192.168.1.1");

        assertThat(signature.isSignedBy("TEST@EXAMPLE.COM")).isTrue();
    }

    @Test
    void 같은_값을_가진_Signature는_동등하다() {
        LocalDateTime now = LocalDateTime.now();

        Signature signature1 = Signature.create("test@example.com", "홍길동", "서명데이터", "192.168.1.1");

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Signature signature2 = Signature.create("test@example.com", "홍길동", "서명데이터", "192.168.1.1");

        assertThat(signature1.getSignerEmail()).isEqualTo(signature2.getSignerEmail());
        assertThat(signature1.getSignerName()).isEqualTo(signature2.getSignerName());
        assertThat(signature1.getSignatureData()).isEqualTo(signature2.getSignatureData());
        assertThat(signature1.getIpAddress()).isEqualTo(signature2.getIpAddress());
    }
}