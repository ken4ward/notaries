package com.christdoes.notaris.service;

import com.christdoes.notaris.repository.UserProfileRepository;
import com.christdoes.notaris.service.dto.UserProfileDTO;
import com.christdoes.notaris.service.mapper.UserProfileMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.christdoes.notaris.domain.UserProfile}.
 */
@Service
@Transactional
public class UserProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileService.class);

    private final UserProfileRepository userProfileRepository;

    private final UserProfileMapper userProfileMapper;

    public UserProfileService(UserProfileRepository userProfileRepository, UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * Save a userProfile.
     *
     * @param userProfileDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<UserProfileDTO> save(UserProfileDTO userProfileDTO) {
        LOG.debug("Request to save UserProfile : {}", userProfileDTO);
        return userProfileRepository.save(userProfileMapper.toEntity(userProfileDTO)).map(userProfileMapper::toDto);
    }

    /**
     * Update a userProfile.
     *
     * @param userProfileDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<UserProfileDTO> update(UserProfileDTO userProfileDTO) {
        LOG.debug("Request to update UserProfile : {}", userProfileDTO);
        return userProfileRepository.save(userProfileMapper.toEntity(userProfileDTO).setIsPersisted()).map(userProfileMapper::toDto);
    }

    /**
     * Partially update a userProfile.
     *
     * @param userProfileDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<UserProfileDTO> partialUpdate(UserProfileDTO userProfileDTO) {
        LOG.debug("Request to partially update UserProfile : {}", userProfileDTO);

        return userProfileRepository
            .findById(userProfileDTO.getId())
            .map(existingUserProfile -> {
                userProfileMapper.partialUpdate(existingUserProfile, userProfileDTO);

                return existingUserProfile;
            })
            .flatMap(userProfileRepository::save)
            .map(userProfileMapper::toDto);
    }

    /**
     * Get all the userProfiles.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<UserProfileDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all UserProfiles");
        return userProfileRepository.findAllBy(pageable).map(userProfileMapper::toDto);
    }

    /**
     * Returns the number of userProfiles available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return userProfileRepository.count();
    }

    /**
     * Get one userProfile by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<UserProfileDTO> findOne(UUID id) {
        LOG.debug("Request to get UserProfile : {}", id);
        return userProfileRepository.findById(id).map(userProfileMapper::toDto);
    }

    /**
     * Delete the userProfile by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete UserProfile : {}", id);
        return userProfileRepository.deleteById(id);
    }
}
