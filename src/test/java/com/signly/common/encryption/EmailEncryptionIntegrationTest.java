package com.signly.common.encryption;

import com.signly.common.util.UlidGenerator;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.PresetType;
import com.signly.contract.infrastructure.entity.ContractJpaEntity;
import com.signly.contract.infrastructure.repository.ContractJpaRepository;
import com.signly.user.domain.model.UserStatus;
import com.signly.user.domain.model.UserType;
import com.signly.user.infrastructure.persistence.entity.UserEntity;
import com.signly.user.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EncryptionConfig.class)
@TestPropertySource(properties = {
    "app.encryption.enabled=true",
    "app.encryption.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=",
    "app.encryption.salt=testSaltForIntegrationTest123"
})
@DisplayName("이메일 암호화 및 해시 통합 테스트")
class EmailEncryptionIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ContractJpaRepository contractRepository;

    @Autowired
    private AesEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        contractRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    // ==================== User Email Hash Tests ====================

    @Test
    @DisplayName("사용자 저장 시 이메일이 암호화되고 해시가 생성된다")
    void user_email_encrypted_and_hashed() {
        // Given
        String plainEmail = "test@example.com";
        String emailHash = encryptionService.hashEmail(plainEmail);

        UserEntity user = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W1", plainEmail, emailHash);

        // When
        UserEntity savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<UserEntity> found = userRepository.findById(savedUser.getUserId());
        assertTrue(found.isPresent());

        UserEntity loadedUser = found.get();
        assertEquals(plainEmail, loadedUser.getEmail()); // 복호화된 이메일
        assertEquals(emailHash, loadedUser.getEmailHash()); // 해시는 그대로
    }

    @Test
    @DisplayName("데이터베이스에 저장된 이메일은 암호화되어 있다")
    void user_email_stored_encrypted_in_database() {
        // Given
        String plainEmail = "test@example.com";
        String emailHash = encryptionService.hashEmail(plainEmail);

        UserEntity user = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W2", plainEmail, emailHash);

        // When
        userRepository.save(user);
        entityManager.flush();

        // Then - Direct database query
        String encryptedEmail = (String) entityManager.getEntityManager()
            .createNativeQuery("SELECT email FROM users WHERE user_id = :userId")
            .setParameter("userId", user.getUserId())
            .getSingleResult();

        // 암호화된 이메일은 평문과 다르다
        assertNotEquals(plainEmail, encryptedEmail);

        // {ENC} prefix가 있다
        assertTrue(encryptedEmail.startsWith("{ENC}"), "암호화된 데이터는 {ENC} prefix를 가져야 함");

        // {ENC} prefix 제거 후 Base64로 디코딩 가능하다
        String withoutPrefix = encryptedEmail.substring(5);
        assertDoesNotThrow(() -> Base64.getDecoder().decode(withoutPrefix));

        // 복호화하면 원본 이메일이 나온다
        String decrypted = encryptionService.decrypt(encryptedEmail);
        assertEquals(plainEmail, decrypted);
    }

    @Test
    @DisplayName("이메일 해시로 사용자를 찾을 수 있다")
    void find_user_by_email_hash() {
        // Given
        String email1 = "test1@example.com";
        String email2 = "test2@example.com";

        String hash1 = encryptionService.hashEmail(email1);
        String hash2 = encryptionService.hashEmail(email2);

        UserEntity user1 = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W3", email1, hash1);
        UserEntity user2 = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W4", email2, hash2);

        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<UserEntity> found1 = userRepository.findByEmailHash(hash1);
        Optional<UserEntity> found2 = userRepository.findByEmailHash(hash2);

        // Then
        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals(email1, found1.get().getEmail());
        assertEquals(email2, found2.get().getEmail());
    }

    @Test
    @DisplayName("이메일 해시 검색은 대소문자를 구분하지 않는다")
    void find_user_by_email_hash_case_insensitive() {
        // Given
        String email = "Test@Example.COM";
        String emailHash = encryptionService.hashEmail(email); // 내부적으로 toLowerCase() 됨

        UserEntity user = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W5", email, emailHash);
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // When - 다른 케이스로 검색
        String searchHash1 = encryptionService.hashEmail("test@example.com");
        String searchHash2 = encryptionService.hashEmail("TEST@EXAMPLE.COM");
        String searchHash3 = encryptionService.hashEmail("TeSt@ExAmPlE.cOm");

        // Then
        Optional<UserEntity> found1 = userRepository.findByEmailHash(searchHash1);
        Optional<UserEntity> found2 = userRepository.findByEmailHash(searchHash2);
        Optional<UserEntity> found3 = userRepository.findByEmailHash(searchHash3);

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertTrue(found3.isPresent());

        // 모두 같은 사용자를 찾아야 함
        assertEquals(user.getUserId(), found1.get().getUserId());
        assertEquals(user.getUserId(), found2.get().getUserId());
        assertEquals(user.getUserId(), found3.get().getUserId());
    }

    @Test
    @DisplayName("동일한 이메일을 가진 사용자는 같은 해시를 갖는다")
    void same_email_same_hash() {
        // Given
        String email = "duplicate@example.com";
        String hash = encryptionService.hashEmail(email);

        UserEntity user1 = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W6", email, hash);

        // When
        userRepository.save(user1);
        entityManager.flush();

        // Then - 같은 이메일로 두 번째 사용자 생성 시도 (유니크 제약 위반 예상)
        UserEntity user2 = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W7", email, hash);

        // 같은 emailHash를 가지므로 unique constraint violation 발생
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            entityManager.flush();
        });
    }

    // ==================== Contract Email Hash Tests ====================

    @Test
    @DisplayName("계약 저장 시 이메일이 암호화되고 해시가 생성된다")
    void contract_email_encrypted_and_hashed() {
        // Given
        String firstPartyEmail = "first@example.com";
        String secondPartyEmail = "second@example.com";

        String firstPartyHash = encryptionService.hashEmail(firstPartyEmail);
        String secondPartyHash = encryptionService.hashEmail(secondPartyEmail);

        ContractJpaEntity contract = createTestContract(
            firstPartyEmail, secondPartyEmail,
            firstPartyHash, secondPartyHash
        );

        // When
        ContractJpaEntity savedContract = contractRepository.save(contract);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<ContractJpaEntity> found = contractRepository.findById(savedContract.getId());
        assertTrue(found.isPresent());

        ContractJpaEntity loadedContract = found.get();
        assertEquals(firstPartyEmail, loadedContract.getFirstPartyEmail());
        assertEquals(secondPartyEmail, loadedContract.getSecondPartyEmail());
        assertEquals(firstPartyHash, loadedContract.getFirstPartyEmailHash());
        assertEquals(secondPartyHash, loadedContract.getSecondPartyEmailHash());
    }

    @Test
    @DisplayName("데이터베이스에 저장된 계약 이메일은 암호화되어 있다")
    void contract_email_stored_encrypted_in_database() {
        // Given
        String firstPartyEmail = "first@example.com";
        String secondPartyEmail = "second@example.com";

        String firstPartyHash = encryptionService.hashEmail(firstPartyEmail);
        String secondPartyHash = encryptionService.hashEmail(secondPartyEmail);

        ContractJpaEntity contract = createTestContract(
            firstPartyEmail, secondPartyEmail,
            firstPartyHash, secondPartyHash
        );

        // When
        contractRepository.save(contract);
        entityManager.flush();

        // Then - Direct database query
        Object[] result = (Object[]) entityManager.getEntityManager()
            .createNativeQuery("SELECT first_party_email, second_party_email FROM contracts WHERE id = :id")
            .setParameter("id", contract.getId())
            .getSingleResult();

        String encryptedFirstEmail = (String) result[0];
        String encryptedSecondEmail = (String) result[1];

        // 암호화된 이메일은 평문과 다르다
        assertNotEquals(firstPartyEmail, encryptedFirstEmail);
        assertNotEquals(secondPartyEmail, encryptedSecondEmail);

        // {ENC} prefix가 있다
        assertTrue(encryptedFirstEmail.startsWith("{ENC}"), "암호화된 데이터는 {ENC} prefix를 가져야 함");
        assertTrue(encryptedSecondEmail.startsWith("{ENC}"), "암호화된 데이터는 {ENC} prefix를 가져야 함");

        // {ENC} prefix 제거 후 Base64로 디코딩 가능하다
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encryptedFirstEmail.substring(5)));
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encryptedSecondEmail.substring(5)));

        // 복호화하면 원본 이메일이 나온다
        assertEquals(firstPartyEmail, encryptionService.decrypt(encryptedFirstEmail));
        assertEquals(secondPartyEmail, encryptionService.decrypt(encryptedSecondEmail));
    }

    @Test
    @DisplayName("이메일 해시로 계약을 찾을 수 있다 (당사자 이메일 검색)")
    void find_contract_by_party_email_hash() {
        // Given
        String email = "party@example.com";
        String emailHash = encryptionService.hashEmail(email);

        // 첫 번째 계약 - firstParty가 해당 이메일
        ContractJpaEntity contract1 = createTestContract(
            email, "other1@example.com",
            emailHash, encryptionService.hashEmail("other1@example.com")
        );

        // 두 번째 계약 - secondParty가 해당 이메일
        ContractJpaEntity contract2 = createTestContract(
            "other2@example.com", email,
            encryptionService.hashEmail("other2@example.com"), emailHash
        );

        contractRepository.save(contract1);
        contractRepository.save(contract2);
        entityManager.flush();
        entityManager.clear();

        // When - 해시로 검색 (Custom query 필요)
        // Note: 실제로는 ContractJpaRepository에 findByPartyEmailHash 메서드가 있어야 함

        // Then - Native query로 확인
        Long count = (Long) entityManager.getEntityManager()
            .createNativeQuery("SELECT COUNT(*) FROM contracts WHERE first_party_email_hash = :hash OR second_party_email_hash = :hash")
            .setParameter("hash", emailHash)
            .getSingleResult();

        assertEquals(2L, count);
    }

    // ==================== Helper Methods ====================

    private UserEntity createTestUser(String userId, String email, String emailHash) {
        LocalDateTime now = LocalDateTime.now();
        return new UserEntity(
            userId,
            email,
            emailHash,
            "encodedPassword",
            "Test User",
            "Test Company",
            "010-1234-5678",
            "서울시 강남구 테헤란로 123",
            UserType.OWNER,
            UserStatus.ACTIVE,
            true,
            null,
            null,
            now,
            now
        );
    }

    private ContractJpaEntity createTestContract(
        String firstPartyEmail,
        String secondPartyEmail,
        String firstPartyHash,
        String secondPartyHash
    ) {
        String contractId = UlidGenerator.generate();
        ContractJpaEntity contract = new ContractJpaEntity(
            contractId,
            "01H8X9Y2Z3W4V5U6T7R8S9Q0C1", // creatorId
            null,
            "Test Contract",
            "Contract content",
            null,
            "First Party",
            firstPartyEmail,
            "First Org",
            "Second Party",
            secondPartyEmail,
            "Second Org",
            ContractStatus.PENDING,
            UlidGenerator.generate(), // signToken
            LocalDateTime.now().plusDays(30),
            PresetType.NONE
        );

        contract.setFirstPartyEmailHash(firstPartyHash);
        contract.setSecondPartyEmailHash(secondPartyHash);

        return contract;
    }
}
