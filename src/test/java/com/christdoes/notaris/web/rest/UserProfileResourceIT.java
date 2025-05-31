package com.christdoes.notaris.web.rest;

import static com.christdoes.notaris.domain.UserProfileAsserts.*;
import static com.christdoes.notaris.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.christdoes.notaris.IntegrationTest;
import com.christdoes.notaris.domain.UserProfile;
import com.christdoes.notaris.repository.EntityManager;
import com.christdoes.notaris.repository.UserProfileRepository;
import com.christdoes.notaris.service.dto.UserProfileDTO;
import com.christdoes.notaris.service.mapper.UserProfileMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link UserProfileResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class UserProfileResourceIT {

    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_USERNAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "E5K\"s@B6Yw;R.Xie";
    private static final String UPDATED_EMAIL = "tSk)@'G.`di";

    private static final Integer DEFAULT_AGE = 1;
    private static final Integer UPDATED_AGE = 2;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/user-profiles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private UserProfile userProfile;

    private UserProfile insertedUserProfile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserProfile createEntity() {
        return new UserProfile()
            .id(UUID.randomUUID())
            .username(DEFAULT_USERNAME)
            .email(DEFAULT_EMAIL)
            .age(DEFAULT_AGE)
            .createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserProfile createUpdatedEntity() {
        return new UserProfile()
            .id(UUID.randomUUID())
            .username(UPDATED_USERNAME)
            .email(UPDATED_EMAIL)
            .age(UPDATED_AGE)
            .createdAt(UPDATED_CREATED_AT);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(UserProfile.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        userProfile = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedUserProfile != null) {
            userProfileRepository.delete(insertedUserProfile).block();
            insertedUserProfile = null;
        }
        deleteEntities(em);
    }

    @Test
    void createUserProfile() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        userProfile.setId(null);
        // Create the UserProfile
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);
        var returnedUserProfileDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(UserProfileDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the UserProfile in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedUserProfile = userProfileMapper.toEntity(returnedUserProfileDTO);
        assertUserProfileUpdatableFieldsEquals(returnedUserProfile, getPersistedUserProfile(returnedUserProfile));

        insertedUserProfile = returnedUserProfile;
    }

    @Test
    void createUserProfileWithExistingId() throws Exception {
        // Create the UserProfile with an existing ID
        insertedUserProfile = userProfileRepository.save(userProfile).block();
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkUsernameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userProfile.setUsername(null);

        // Create the UserProfile, which fails.
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userProfile.setEmail(null);

        // Create the UserProfile, which fails.
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        userProfile.setCreatedAt(null);

        // Create the UserProfile, which fails.
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllUserProfiles() {
        // Initialize the database
        userProfile.setId(UUID.randomUUID());
        insertedUserProfile = userProfileRepository.save(userProfile).block();

        // Get all the userProfileList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(userProfile.getId().toString()))
            .jsonPath("$.[*].username")
            .value(hasItem(DEFAULT_USERNAME))
            .jsonPath("$.[*].email")
            .value(hasItem(DEFAULT_EMAIL))
            .jsonPath("$.[*].age")
            .value(hasItem(DEFAULT_AGE))
            .jsonPath("$.[*].createdAt")
            .value(hasItem(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    void getUserProfile() {
        // Initialize the database
        userProfile.setId(UUID.randomUUID());
        insertedUserProfile = userProfileRepository.save(userProfile).block();

        // Get the userProfile
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, userProfile.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(userProfile.getId().toString()))
            .jsonPath("$.username")
            .value(is(DEFAULT_USERNAME))
            .jsonPath("$.email")
            .value(is(DEFAULT_EMAIL))
            .jsonPath("$.age")
            .value(is(DEFAULT_AGE))
            .jsonPath("$.createdAt")
            .value(is(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    void getNonExistingUserProfile() {
        // Get the userProfile
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingUserProfile() throws Exception {
        // Initialize the database
        userProfile.setId(UUID.randomUUID());
        insertedUserProfile = userProfileRepository.save(userProfile).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userProfile
        UserProfile updatedUserProfile = userProfileRepository.findById(userProfile.getId()).block();
        updatedUserProfile.username(UPDATED_USERNAME).email(UPDATED_EMAIL).age(UPDATED_AGE).createdAt(UPDATED_CREATED_AT);
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(updatedUserProfile);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, userProfileDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedUserProfileToMatchAllProperties(updatedUserProfile);
    }

    @Test
    void putNonExistingUserProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userProfile.setId(UUID.randomUUID());

        // Create the UserProfile
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, userProfileDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchUserProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userProfile.setId(UUID.randomUUID());

        // Create the UserProfile
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamUserProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userProfile.setId(UUID.randomUUID());

        // Create the UserProfile
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the UserProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateUserProfileWithPatch() throws Exception {
        // Initialize the database
        userProfile.setId(UUID.randomUUID());
        insertedUserProfile = userProfileRepository.save(userProfile).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userProfile using partial update
        UserProfile partialUpdatedUserProfile = new UserProfile();
        partialUpdatedUserProfile.setId(userProfile.getId());

        partialUpdatedUserProfile.age(UPDATED_AGE).createdAt(UPDATED_CREATED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedUserProfile.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedUserProfile))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserProfileUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedUserProfile, userProfile),
            getPersistedUserProfile(userProfile)
        );
    }

    @Test
    void fullUpdateUserProfileWithPatch() throws Exception {
        // Initialize the database
        userProfile.setId(UUID.randomUUID());
        insertedUserProfile = userProfileRepository.save(userProfile).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the userProfile using partial update
        UserProfile partialUpdatedUserProfile = new UserProfile();
        partialUpdatedUserProfile.setId(userProfile.getId());

        partialUpdatedUserProfile.username(UPDATED_USERNAME).email(UPDATED_EMAIL).age(UPDATED_AGE).createdAt(UPDATED_CREATED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedUserProfile.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedUserProfile))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the UserProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserProfileUpdatableFieldsEquals(partialUpdatedUserProfile, getPersistedUserProfile(partialUpdatedUserProfile));
    }

    @Test
    void patchNonExistingUserProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userProfile.setId(UUID.randomUUID());

        // Create the UserProfile
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, userProfileDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchUserProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userProfile.setId(UUID.randomUUID());

        // Create the UserProfile
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the UserProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamUserProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        userProfile.setId(UUID.randomUUID());

        // Create the UserProfile
        UserProfileDTO userProfileDTO = userProfileMapper.toDto(userProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(userProfileDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the UserProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteUserProfile() {
        // Initialize the database
        userProfile.setId(UUID.randomUUID());
        insertedUserProfile = userProfileRepository.save(userProfile).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the userProfile
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, userProfile.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return userProfileRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected UserProfile getPersistedUserProfile(UserProfile userProfile) {
        return userProfileRepository.findById(userProfile.getId()).block();
    }

    protected void assertPersistedUserProfileToMatchAllProperties(UserProfile expectedUserProfile) {
        // Test fails because reactive api returns an empty object instead of null
        // assertUserProfileAllPropertiesEquals(expectedUserProfile, getPersistedUserProfile(expectedUserProfile));
        assertUserProfileUpdatableFieldsEquals(expectedUserProfile, getPersistedUserProfile(expectedUserProfile));
    }

    protected void assertPersistedUserProfileToMatchUpdatableProperties(UserProfile expectedUserProfile) {
        // Test fails because reactive api returns an empty object instead of null
        // assertUserProfileAllUpdatablePropertiesEquals(expectedUserProfile, getPersistedUserProfile(expectedUserProfile));
        assertUserProfileUpdatableFieldsEquals(expectedUserProfile, getPersistedUserProfile(expectedUserProfile));
    }
}
