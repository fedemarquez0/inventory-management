package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u LEFT JOIN u.storePermissions sp " +
           "WHERE u.username = :username AND (u.role = 'ADMIN' OR sp.store.id = :storeId)")
    boolean hasStorePermission(@Param("username") String username, @Param("storeId") Long storeId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.storePermissions sp " +
           "WHERE u.username = :username")
    Optional<User> findByUsernameWithPermissions(@Param("username") String username);
}
