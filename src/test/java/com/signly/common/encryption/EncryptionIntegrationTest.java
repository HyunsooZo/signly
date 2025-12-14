package com.signly.common.encryption;

import com.signly.user.domain.model.UserStatus;
import com.signly.user.domain.model.UserType;
import com.signly.user.infrastructure.persistence.entity.UserEntity;
import com.signly.user.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({EncryptionConfig.class, EncryptionProperties.class})
@TestPropertySource(properties = {
    "app.encryption.enabled=true",
    "app.encryption.secret-key=" + "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=" // Base64 of "12345678901234567890123456789012"
})
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
        testUser = new UserEntity();
        testUser.setUserId("01H8X9Y2Z3W4V5U6T7R8S9Q0W1E");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("Test User");
        testUser.setCompanyName("Test Company");
        testUser.setBusinessPhone("010-1234-5678");
        testUser.setBusinessAddress("서울시 강남구 테헤란로 123");
        testUser.setUserType(UserType.OWNER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setEmailVerified(true);
    }

    @Test
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

        // Verify that the data is Base64 encoded (indicates encryption)
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encryptedPhone));
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encryptedAddress));

        // Verify that the data can be decrypted
        String decryptedPhone = encryptionService.decrypt(encryptedPhone);
        String decryptedAddress = encryptionService.decrypt(encryptedAddress);

        assertEquals(testUser.getBusinessPhone(), decryptedPhone);
        assertEquals(testUser.getBusinessAddress(), decryptedAddress);
    }

    @Test
    void multiple_users_same_data_different_encrypted_values() {
        // Given
        UserEntity user1 = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W1A");
        UserEntity user2 = createTestUser("01H8X9Y2Z3W4V5U6T7R8S9Q0W1B");

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
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setEmail("test" + userId + "@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setCompanyName("Test Company");
        user.setUserType(UserType.OWNER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        return user;
    }
}