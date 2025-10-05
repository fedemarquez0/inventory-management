package com.meli.inventorymanagement.infrastructure.adapter.output.persistence;

import com.meli.inventorymanagement.domain.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p JOIN FETCH i.store s WHERE p.sku = :sku")
    List<Inventory> findByProductSku(@Param("sku") String sku);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p JOIN FETCH i.store s WHERE p.sku = :sku AND s.id = :storeId")
    Optional<Inventory> findByProductSkuAndStoreId(@Param("sku") String sku, @Param("storeId") Long storeId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT i FROM Inventory i WHERE i.id = :id")
    Optional<Inventory> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.store.id = :storeId")
    Optional<Inventory> findByProductIdAndStoreId(@Param("productId") Long productId, @Param("storeId") Long storeId);
}
