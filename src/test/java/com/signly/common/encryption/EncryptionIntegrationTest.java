package com.signly.common.encryption;

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
    "app.encryption.secret-key=" + "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=", // Base64 of "12345678901234567890123456789012"
    "app.encryption.salt=test-salt-for-hashing"
})
@DisplayName("암호화 통합 테스트")
class EncryptionIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private AesEncryptionService encryptionService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // 이전 테스트 데이터 정리
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        LocalDateTime now = LocalDateTime.now();
        testUser = UserEntity.builder()
            .userId("01H8X9Y2Z3W4V5U6T7R8S9Q0W1")
            .email("test@example.com")
            .emailHash("emailHashPlaceholder")
            .password("encodedPassword")
            .name("Test User")
            .companyName("Test Company")
            .businessPhone("010-1234-5678")
            .businessAddress("서울시 강남구 테헤란로 123")
            .userType(UserType.OWNER)
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .verificationToken("verificationToken")
            .verificationTokenExpiry(now)
            .failedLoginAttempts(0)
            .lastFailedLoginAt(null)
            .accountLockedAt(null)
            .unlockToken("unlockToken")
            .unlockTokenExpiry(now)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    @Test
    @DisplayName("암호화된 필드를 포함한 사용자 엔티티를 저장하고 로드할 수 있다")
    void userEntity_save_and_load_encrypted_fields() {
        // When
        UserEntity savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getUserId());
        assertTrue(foundUser.isPresent());

        UserEntity loadedUser = foundUser.get();
        assertEquals(testUser.getBusinessPhone(), loadedUser.getBusinessPhone());
        assertEquals(testUser.getBusinessAddress(), loadedUser.getBusinessAddress());
        assertEquals(testUser.getEmail(), loadedUser.getEmail()); // Not encrypted
        assertEquals(testUser.getName(), loadedUser.getName()); // Not encrypted
    }

    @Test
    @DisplayName("데이터베이스에는 암호화된 값이 저장되고 평문은 저장되지 않는다")
    void database_contains_encrypted_not_plaintext() {
        // When
        UserEntity savedUser = userRepository.save(testUser);
        entityManager.flush();

        // Then - Direct database query to check encrypted values
        String encryptedPhone = (String) entityManager.getEntityManager()
            .createNativeQuery("SELECT business_phone FROM users WHERE user_id = :userId")
            .setParameter("userId", savedUser.getUserId())
            .getSingleResult();

        String encryptedAddress = (String) entityManager.getEntityManager()
            .createNativeQuery("SELECT business_address FROM users WHERE user_id = :userId")
            .setParameter("userId", savedUser.getUserId())
            .getSingleResult();

        // Verify that the data is encrypted (not equal to plaintext)
        assertNotEquals(testUser.getBusinessPhone(), encryptedPhone);
        assertNotEquals(testUser.getBusinessAddress(), encryptedAddress);

        // Verify that the data has {ENC} prefix
        assertTrue(encryptedPhone.startsWith("{ENC}"), "암호화된 데이터는 {ENC} prefix를 가져야 함");
        assertTrue(encryptedAddress.startsWith("{ENC}"), "암호화된 데이터는 {ENC} prefix를 가져야 함");

        // Verify that the data is Base64 encoded after removing prefix
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encryptedPhone.substring(5)));
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encryptedAddress.substring(5)));

        // Verify that the data can be decrypted
        String decryptedPhone = encryptionService.decrypt(encryptedPhone);
        String decryptedAddress = encryptionService.decrypt(encryptedAddress);

        assertEquals(testUser.getBusinessPhone(), decryptedPhone);
        assertEquals(testUser.getBusinessAddress(), decryptedAddress);
    }

    @Test
    @DisplayName("동일한 데이터를 가진 여러 사용자는 서로 다른 암호문을 갖는다")
    void multiple_users_same_data_different_encrypted_values() {
        // Given
        UserEntity user1 = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0WA");
        UserEntity user2 = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0WB");

        user1.setBusinessPhone("010-1234-5678");
        user1.setBusinessAddress("서울시 강남구 테헤란로 123");

        user2.setBusinessPhone("010-1234-5678"); // Same phone number
        user2.setBusinessAddress("서울시 강남구 테헤란로 123"); // Same address

        // When
        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();

        // Then - Same plaintext should result in different ciphertext
        String encryptedPhone1 = (String) entityManager.getEntityManager()
            .createNativeQuery("SELECT business_phone FROM users WHERE user_id = :userId")
            .setParameter("userId", user1.getUserId())
            .getSingleResult();

        String encryptedPhone2 = (String) entityManager.getEntityManager()
            .createNativeQuery("SELECT business_phone FROM users WHERE user_id = :userId")
            .setParameter("userId", user2.getUserId())
            .getSingleResult();

        String encryptedAddress1 = (String) entityManager.getEntityManager()
            .createNativeQuery("SELECT business_address FROM users WHERE user_id = :userId")
            .setParameter("userId", user1.getUserId())
            .getSingleResult();

        String encryptedAddress2 = (String) entityManager.getEntityManager()
            .createNativeQuery("SELECT business_address FROM users WHERE user_id = :userId")
            .setParameter("userId", user2.getUserId())
            .getSingleResult();

        // Same plaintext should produce different ciphertext (due to random IV)
        assertNotEquals(encryptedPhone1, encryptedPhone2);
        assertNotEquals(encryptedAddress1, encryptedAddress2);

        // But both should decrypt to the same value
        assertEquals(user1.getBusinessPhone(), encryptionService.decrypt(encryptedPhone1));
        assertEquals(user2.getBusinessPhone(), encryptionService.decrypt(encryptedPhone2));
        assertEquals(user1.getBusinessAddress(), encryptionService.decrypt(encryptedAddress1));
        assertEquals(user2.getBusinessAddress(), encryptionService.decrypt(encryptedAddress2));
    }

    @Test
    @DisplayName("null 값도 정상적으로 처리된다")
    void null_values_handled_correctly() {
        // Given
        testUser.setBusinessPhone(null);
        testUser.setBusinessAddress(null);

        // When
        UserEntity savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getUserId());
        assertTrue(foundUser.isPresent());

        UserEntity loadedUser = foundUser.get();
        assertNull(loadedUser.getBusinessPhone());
        assertNull(loadedUser.getBusinessAddress());
    }

    @Test
    @DisplayName("빈 문자열도 정상적으로 처리된다")
    void empty_values_handled_correctly() {
        // Given
        testUser.setBusinessPhone("");
        testUser.setBusinessAddress("");

        // When
        UserEntity savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getUserId());
        assertTrue(foundUser.isPresent());

        UserEntity loadedUser = foundUser.get();
        assertEquals("", loadedUser.getBusinessPhone());
        assertEquals("", loadedUser.getBusinessAddress());
    }

    @Test
    @DisplayName("암호화된 필드를 업데이트할 수 있다")
    void update_encrypted_field() {
        // When
        UserEntity savedUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // Update the encrypted field
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getUserId());
        assertTrue(foundUser.isPresent());

        UserEntity loadedUser = foundUser.get();
        loadedUser.setBusinessPhone("010-9876-5432");
        loadedUser.setBusinessAddress("부산시 해운대구 광안리 456");

        userRepository.save(loadedUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<UserEntity> updatedUser = userRepository.findById(savedUser.getUserId());
        assertTrue(updatedUser.isPresent());

        UserEntity finalUser = updatedUser.get();
        assertEquals("010-9876-5432", finalUser.getBusinessPhone());
        assertEquals("부산시 해운대구 광안리 456", finalUser.getBusinessAddress());
    }

    private UserEntity createTestUser(String userId) {
        LocalDateTime now = LocalDateTime.now();
        String email = "test" + userId + "@example.com";
        String emailHash = encryptionService.hashEmail(email);
        return UserEntity.builder()
            .userId(userId)
            .email(email)
            .emailHash(emailHash)
            .password("encodedPassword")
            .name("Test User")
            .companyName("Test Company")
            .businessPhone(null)
            .businessAddress(null)
            .userType(UserType.OWNER)
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .verificationToken("verificationToken")
            .verificationTokenExpiry(now)
            .failedLoginAttempts(0)
            .lastFailedLoginAt(null)
            .accountLockedAt(null)
            .unlockToken("unlockToken")
            .unlockTokenExpiry(now)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }
}