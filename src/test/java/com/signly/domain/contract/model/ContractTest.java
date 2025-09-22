package com.signly.domain.contract.model;

import com.signly.common.exception.ValidationException;
import com.signly.domain.template.model.TemplateId;
import com.signly.domain.user.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class ContractTest {

    private UserId creatorId;
    private TemplateId templateId;
    private String title;
    private ContractContent content;
    private PartyInfo firstParty;
    private PartyInfo secondParty;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
        creatorId = UserId.generate();
        templateId = TemplateId.generate();
        title = "테스트 계약서";
        content = ContractContent.of("계약서 내용입니다.");
        firstParty = PartyInfo.of("홍길동", "first@example.com", "회사A");
        secondParty = PartyInfo.of("김철수", "second@example.com", "회사B");
        expiresAt = LocalDateTime.now().plusDays(30);
    }

    @Test
    void 정상적인_정보로_계약서를_생성할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);

        assertThat(contract.getId()).isNotNull();
        assertThat(contract.getCreatorId()).isEqualTo(creatorId);
        assertThat(contract.getTemplateId()).isEqualTo(templateId);
        assertThat(contract.getTitle()).isEqualTo(title);
        assertThat(contract.getContent()).isEqualTo(content);
        assertThat(contract.getFirstParty()).isEqualTo(firstParty);
        assertThat(contract.getSecondParty()).isEqualTo(secondParty);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.DRAFT);
        assertThat(contract.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(contract.getSignatures()).isEmpty();
        assertThat(contract.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(contract.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void 템플릿_없이_계약서를_생성할_수_있다() {
        Contract contract = Contract.create(creatorId, null, title, content, firstParty, secondParty, expiresAt);

        assertThat(contract.getTemplateId()).isNull();
    }

    @Test
    void 만료일_없이_계약서를_생성할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, null);

        assertThat(contract.getExpiresAt()).isNull();
    }

    @Test
    void null_제목으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Contract.create(creatorId, templateId, null, content, firstParty, secondParty, expiresAt))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 제목은 필수입니다");
    }

    @Test
    void 빈_제목으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> Contract.create(creatorId, templateId, "", content, firstParty, secondParty, expiresAt))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 제목은 필수입니다");
    }

    @Test
    void 너무_긴_제목으로_생성하면_예외가_발생한다() {
        String longTitle = "a".repeat(201);

        assertThatThrownBy(() -> Contract.create(creatorId, templateId, longTitle, content, firstParty, secondParty, expiresAt))
                .isInstanceOf(ValidationException.class)
                .hasMessage("계약서 제목은 200자를 초과할 수 없습니다");
    }

    @Test
    void 과거의_만료일로_생성하면_예외가_발생한다() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        assertThatThrownBy(() -> Contract.create(creatorId, templateId, title, content, firstParty, secondParty, pastTime))
                .isInstanceOf(ValidationException.class)
                .hasMessage("만료일은 현재 시간으로부터 최소 1시간 이후여야 합니다");
    }

    @Test
    void 같은_이메일의_당사자로_생성하면_예외가_발생한다() {
        PartyInfo sameEmailParty = PartyInfo.of("다른이름", "first@example.com", "다른회사");

        assertThatThrownBy(() -> Contract.create(creatorId, templateId, title, content, firstParty, sameEmailParty, expiresAt))
                .isInstanceOf(ValidationException.class)
                .hasMessage("당사자들의 이메일은 서로 달라야 합니다");
    }

    @Test
    void 초안_상태에서_제목을_수정할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        String newTitle = "수정된 제목";

        contract.updateTitle(newTitle);

        assertThat(contract.getTitle()).isEqualTo(newTitle);
    }

    @Test
    void 초안_상태에서_내용을_수정할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        ContractContent newContent = ContractContent.of("수정된 내용");

        contract.updateContent(newContent);

        assertThat(contract.getContent()).isEqualTo(newContent);
    }

    @Test
    void 초안_상태에서_만료일을_수정할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(60);

        contract.updateExpirationDate(newExpiresAt);

        assertThat(contract.getExpiresAt()).isEqualTo(newExpiresAt);
    }

    @Test
    void 초안_상태에서_서명_요청을_보낼_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);

        contract.sendForSigning();

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.PENDING);
    }

    @Test
    void 서명_대기_상태에서_서명할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        contract.sendForSigning();

        contract.sign("first@example.com", "홍길동", "서명데이터", "192.168.1.1");

        assertThat(contract.getSignatures()).hasSize(1);
        assertThat(contract.getSignatures().get(0).getSignerEmail()).isEqualTo("first@example.com");
    }

    @Test
    void 모든_당사자가_서명하면_서명완료_상태가_된다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        contract.sendForSigning();

        contract.sign("first@example.com", "홍길동", "서명데이터1", "192.168.1.1");
        contract.sign("second@example.com", "김철수", "서명데이터2", "192.168.1.2");

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.SIGNED);
        assertThat(contract.getSignatures()).hasSize(2);
    }

    @Test
    void 서명완료_상태에서_완료할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        contract.sendForSigning();
        contract.sign("first@example.com", "홍길동", "서명데이터1", "192.168.1.1");
        contract.sign("second@example.com", "김철수", "서명데이터2", "192.168.1.2");

        contract.complete();

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.COMPLETED);
    }

    @Test
    void 초안_상태에서_취소할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);

        contract.cancel();

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.CANCELLED);
    }

    @Test
    void 서명_대기_상태에서_취소할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        contract.sendForSigning();

        contract.cancel();

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.CANCELLED);
    }

    @Test
    void 만료된_계약서는_만료_처리할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);

        contract.expire();

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.EXPIRED);
    }

    @Test
    void 초안_상태의_계약서만_삭제할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);

        assertThat(contract.canDelete()).isTrue();

        contract.sendForSigning();

        assertThat(contract.canDelete()).isFalse();
    }

    @Test
    void 대기중인_서명자_목록을_확인할_수_있다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        contract.sendForSigning();

        assertThat(contract.getPendingSigners()).containsExactlyInAnyOrder("first@example.com", "second@example.com");

        contract.sign("first@example.com", "홍길동", "서명데이터", "192.168.1.1");

        assertThat(contract.getPendingSigners()).containsExactly("second@example.com");
    }

    @Test
    void 권한이_없는_사용자가_서명하면_예외가_발생한다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        contract.sendForSigning();

        assertThatThrownBy(() -> contract.sign("unauthorized@example.com", "무권한자", "서명데이터", "192.168.1.1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("해당 계약서에 서명할 권한이 없습니다");
    }

    @Test
    void 이미_서명한_사용자가_다시_서명하면_예외가_발생한다() {
        Contract contract = Contract.create(creatorId, templateId, title, content, firstParty, secondParty, expiresAt);
        contract.sendForSigning();
        contract.sign("first@example.com", "홍길동", "서명데이터", "192.168.1.1");

        assertThatThrownBy(() -> contract.sign("first@example.com", "홍길동", "서명데이터2", "192.168.1.1"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("이미 서명한 계약서입니다");
    }
}