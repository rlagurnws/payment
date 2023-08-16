package f4.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import f4.product.model.ProductAuctionHistoryEntity;

@Repository
public interface ProductionAuctionHistoryRepository extends JpaRepository<ProductAuctionHistoryEntity, Long>{

}
