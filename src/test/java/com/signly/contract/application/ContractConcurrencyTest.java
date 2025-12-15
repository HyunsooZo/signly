package com.signly.contract.application;

import com.signly.common.exception.ValidationException;
import com.signly.contract.application.dto.SignContractCommand;
import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.ContractId;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.PartyInfo;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.contract.domain.repository.SignatureRepository;
import com.signly.user.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("실제 동시성 테스트는 통합 테스트 환경에서 수행")
class ContractConcurrencyTest {

    @Autowired
    private ContractSigningCoordinator contractSigningCoordinator;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private SignatureRepository signatureRepository;

    private Contract testContract;
    private static final String FIRST_PARTY_EMAIL = "first@example.com";
    private static final String SECOND_PARTY_EMAIL = "second@example.com";
    private static final String SIGNATURE_DATA = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    @BeforeEach
    void setUp() {
        // 테스트용 계약서 생성
        testContract = Contract.create(
                UserId.of("test-user"),
                null,
                "Test Contract",
                com.signly.contract.domain.model.ContractContent.of("Test content"),
                new PartyInfo("First Party", FIRST_PARTY_EMAIL, "First Org"),
                new PartyInfo("Second Party", SECOND_PARTY_EMAIL, "Second Org"),
                LocalDateTime.now().plusDays(7)
        );
        
        testContract.sendForSigning();
        testContract = contractRepository.save(testContract);
    }

    @Test
    @DisplayName("동시 서명 시 두 명 모두 성공해야 함")
    void 동시_서명_시_두명_모두_성공해야함() throws Exception {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        SignContractCommand firstPartyCommand = new SignContractCommand(
                "First Party", SIGNATURE_DATA, "127.0.0.1", "test-device-info"
        );
        
        SignContractCommand secondPartyCommand = new SignContractCommand(
                "Second Party", SIGNATURE_DATA, "127.0.0.1", "test-device-info"
        );

        // When: 두 사람이 동시에 서명
        CompletableFuture<Contract> firstPartyFuture = CompletableFuture.supplyAsync(() -> 
                contractSigningCoordinator.signContract(FIRST_PARTY_EMAIL, testContract.getId().value(), firstPartyCommand),
                executor
        );
        
        CompletableFuture<Contract> secondPartyFuture = CompletableFuture.supplyAsync(() -> 
                contractSigningCoordinator.signContract(SECOND_PARTY_EMAIL, testContract.getId().value(), secondPartyCommand),
                executor
        );

        // Then: 두 서명 모두 성공
        Contract firstResult = firstPartyFuture.get(5, TimeUnit.SECONDS);
        Contract secondResult = secondPartyFuture.get(5, TimeUnit.SECONDS);

        // 최종 상태 확인
        Contract finalContract = contractRepository.findById(testContract.getId()).orElseThrow();
        
        assertThat(finalContract.getStatus()).isEqualTo(ContractStatus.SIGNED);
        assertThat(finalContract.getSignatures()).hasSize(2);
        assertThat(finalContract.isFullySigned()).isTrue();
        
        executor.shutdown();
    }

    @Test
    @DisplayName("동일인 중복 서명 시 ValidationException 발생")
    void 동일인_중복_서명_방지() {
        // Given
        SignContractCommand command = new SignContractCommand(
                "First Party", SIGNATURE_DATA, "127.0.0.1", "test-device-info"
        );

        // When: 첫 번째 서명
        Contract firstResult = contractSigningCoordinator.signContract(
                FIRST_PARTY_EMAIL, testContract.getId().value(), command
        );

        // Then: 두 번째 서명 시도는 실패
        assertThatThrownBy(() -> 
                contractSigningCoordinator.signContract(FIRST_PARTY_EMAIL, testContract.getId().value(), command)
        ).isInstanceOf(ValidationException.class)
         .hasMessageContaining("이미 서명한 계약서입니다");
    }

    @Test
    @DisplayName("낙관적 락 충돌 시 재시도 로직 동작")
    void 낙관적_락_충돌_시_재시도_동작() throws Exception {
        // Given: 수동으로 낙관적 락 충돌 시뮬레이션
        Contract contract = contractRepository.findById(testContract.getId()).orElseThrow();
        
        // 첫 번째 트랜잭션에서 계약서를 조회하고 수정
        Contract contract1 = contractRepository.findById(contract.getId()).orElseThrow();
        
        // 두 번째 트랜잭션에서 동일한 계약서를 조회하고 먼저 저장
        Contract contract2 = contractRepository.findById(contract.getId()).orElseThrow();
        contract2.addSignature(com.signly.contract.domain.model.Signature.create(
                SECOND_PARTY_EMAIL, "Second Party", SIGNATURE_DATA, "127.0.0.1"
        ));
        contractRepository.save(contract2);

        // When: 첫 번째 트랜잭션이 저장 시도
        SignContractCommand command = new SignContractCommand(
                "First Party", SIGNATURE_DATA, "127.0.0.1", "test-device-info"
        );

        // Then: 재시도 로직이 동작하여 최종적으로 성공
        Contract result = contractSigningCoordinator.signContract(
                FIRST_PARTY_EMAIL, testContract.getId().value(), command
        );

        Contract finalContract = contractRepository.findById(testContract.getId()).orElseThrow();
        assertThat(finalContract.getSignatures()).hasSize(2);
        assertThat(finalContract.getStatus()).isEqualTo(ContractStatus.SIGNED);
    }

    @Test
    @DisplayName("여러 스레드에서 동시 서명 요청 시 데이터 무결성 보장")
    void 여러_스레드_동시_서명_시_데이터_무결성_보장() throws Exception {
        // Given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        SignContractCommand command = new SignContractCommand(
                "First Party", SIGNATURE_DATA, "127.0.0.1", "test-device-info"
        );

        // When: 여러 스레드에서 동시에 동일인 서명 시도
        List<CompletableFuture<Contract>> futures = List.of(
            CompletableFuture.supplyAsync(() -> 
                contractSigningCoordinator.signContract(FIRST_PARTY_EMAIL, testContract.getId().value(), command),
                executor
            ),
            CompletableFuture.supplyAsync(() -> 
                contractSigningCoordinator.signContract(FIRST_PARTY_EMAIL, testContract.getId().value(), command),
                executor
            ),
            CompletableFuture.supplyAsync(() -> 
                contractSigningCoordinator.signContract(FIRST_PARTY_EMAIL, testContract.getId().value(), command),
                executor
            )
        );

        // Then: 하나만 성공하고 나머지는 실패
        int successCount = 0;
        int failureCount = 0;
        
        for (CompletableFuture<Contract> future : futures) {
            try {
                future.get(5, TimeUnit.SECONDS);
                successCount++;
            } catch (Exception e) {
                if (e.getCause() instanceof ValidationException) {
                    failureCount++;
                }
            }
        }

        assertThat(successCount).isEqualTo(1);
        assertThat(failureCount).isEqualTo(2);
        
        Contract finalContract = contractRepository.findById(testContract.getId()).orElseThrow();
        assertThat(finalContract.getSignatures()).hasSize(1);
        
        executor.shutdown();
    }
}