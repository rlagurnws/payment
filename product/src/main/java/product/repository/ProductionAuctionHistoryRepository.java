package product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import product.model.ProductAuctionHistoryEntity;

@Repository
public interface ProductionAuctionHistoryRepository extends JpaRepository<ProductAuctionHistoryEntity, Long>{

}
