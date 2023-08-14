package product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import product.model.AuctionProductEntity;

@Repository
public interface AuctionProductRepository extends JpaRepository<AuctionProductEntity,Long>{
	
	public AuctionProductEntity findByProductId(long id);
}
