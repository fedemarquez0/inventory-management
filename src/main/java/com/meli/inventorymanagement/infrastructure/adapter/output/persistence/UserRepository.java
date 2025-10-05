package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<User> findByUsername(String username);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END " +
           "FROM users u " +
           "LEFT JOIN user_store_permissions sp ON u.id = sp.user_id " +
           "WHERE u.username = :username AND (u.role = 'ADMIN' OR sp.store_id = :storeId)")
    Mono<Boolean> hasStorePermission(@Param("username") String username, @Param("storeId") Long storeId);
}
