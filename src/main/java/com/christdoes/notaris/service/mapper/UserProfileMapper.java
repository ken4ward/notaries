package com.christdoes.notaris.service.mapper;

import com.christdoes.notaris.domain.UserProfile;
import com.christdoes.notaris.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link UserProfile} and its DTO {@link UserProfileDTO}.
 */
@Mapper(componentModel = "spring")
public interface UserProfileMapper extends EntityMapper<UserProfileDTO, UserProfile> {}
