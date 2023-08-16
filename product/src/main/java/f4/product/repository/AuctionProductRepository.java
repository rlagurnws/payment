package f4.product.repository;

import f4.product.model.AuctionProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionProductRepository extends JpaRepository<AuctionProductEntity,Long>{
	
	public AuctionProductEntity findByProductId(long id);
}
