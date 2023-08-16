package f4.product.service;

import f4.product.model.AuctionProductEntity;
import f4.product.model.ProductAuctionHistoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import f4.product.model.ProductEntity;
import f4.product.repository.AuctionProductRepository;
import f4.product.repository.ProductRepository;
import f4.product.repository.ProductionAuctionHistoryRepository;

import java.util.Map;

@Service
public class BidService {

	@Autowired
	private AuctionProductRepository repository;
	@Autowired
	private ProductionAuctionHistoryRepository pahr;
	@Autowired
	private ProductRepository pr;
	
	public AuctionProductEntity getAP(String str) {
		return repository.findByProductId(Long.parseLong(str));
	}
	public ProductEntity getPE(String id){
		return pr.findById(Long.parseLong(id)).get();
	}
	public Map<String,Object> immediately(Map<String, Object> map){
		ProductEntity pe = pr.findById(Long.parseLong(map.get("productId").toString())).get();
		pe.setStatus("sold");
		pr.save(pe);
		map.put("status", 1);
		map.put("result", "즉시 구매 완료 되었습니다.");
		return map;
	}
	
	public Map<String,Object> done(Map<String, Object> map, AuctionProductEntity ape) {
		map.put("bidUser", Long.toString(ape.getBidUser()));
		map.put("bidPrice", ape.getBidPrice());
		//Auction 정보 수정
		ape.setBidPrice(map.get("pay").toString());
		ape.setBidUser(Long.parseLong(map.get("userId").toString()));
		repository.save(ape);
		//history 추가
		pahr.save(ProductAuctionHistoryEntity.builder()
											 .productId(ape.getProductId())
											 .bidUser(Long.parseLong(map.get("userId").toString()))
											 .bidPrice(map.get("pay").toString())
											 .build());
		return map;
	}
}
