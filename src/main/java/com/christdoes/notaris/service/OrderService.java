package com.christdoes.notaris.service;

import com.christdoes.notaris.repository.OrderRepository;
import com.christdoes.notaris.service.dto.OrderDTO;
import com.christdoes.notaris.service.mapper.OrderMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.christdoes.notaris.domain.Order}.
 */
@Service
@Transactional
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    /**
     * Save a order.
     *
     * @param orderDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<OrderDTO> save(OrderDTO orderDTO) {
        LOG.debug("Request to save Order : {}", orderDTO);
        return orderRepository.save(orderMapper.toEntity(orderDTO)).map(orderMapper::toDto);
    }

    /**
     * Update a order.
     *
     * @param orderDTO the entity to save.
     * @return the persisted entity.
     */
    public Mono<OrderDTO> update(OrderDTO orderDTO) {
        LOG.debug("Request to update Order : {}", orderDTO);
        return orderRepository.save(orderMapper.toEntity(orderDTO).setIsPersisted()).map(orderMapper::toDto);
    }

    /**
     * Partially update a order.
     *
     * @param orderDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<OrderDTO> partialUpdate(OrderDTO orderDTO) {
        LOG.debug("Request to partially update Order : {}", orderDTO);

        return orderRepository
            .findById(orderDTO.getId())
            .map(existingOrder -> {
                orderMapper.partialUpdate(existingOrder, orderDTO);

                return existingOrder;
            })
            .flatMap(orderRepository::save)
            .map(orderMapper::toDto);
    }

    /**
     * Get all the orders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<OrderDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Orders");
        return orderRepository.findAllBy(pageable).map(orderMapper::toDto);
    }

    /**
     * Get all the orders with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Flux<OrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        return orderRepository.findAllWithEagerRelationships(pageable).map(orderMapper::toDto);
    }

    /**
     * Returns the number of orders available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return orderRepository.count();
    }

    /**
     * Get one order by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<OrderDTO> findOne(UUID id) {
        LOG.debug("Request to get Order : {}", id);
        return orderRepository.findOneWithEagerRelationships(id).map(orderMapper::toDto);
    }

    /**
     * Delete the order by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete Order : {}", id);
        return orderRepository.deleteById(id);
    }
}
