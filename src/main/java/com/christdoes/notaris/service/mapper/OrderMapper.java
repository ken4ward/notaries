package com.christdoes.notaris.service.mapper;

import com.christdoes.notaris.domain.Order;
import com.christdoes.notaris.domain.UserProfile;
import com.christdoes.notaris.service.dto.OrderDTO;
import com.christdoes.notaris.service.dto.UserProfileDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Order} and its DTO {@link OrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrderDTO, Order> {
    @Mapping(target = "userProfile", source = "userProfile", qualifiedByName = "userProfileUsername")
    OrderDTO toDto(Order s);

    @Named("userProfileUsername")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    UserProfileDTO toDtoUserProfileUsername(UserProfile userProfile);
}
