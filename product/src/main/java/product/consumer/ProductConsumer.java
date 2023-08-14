package product.consumer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import product.model.AuctionProductEntity;
import product.model.ProductAuctionHistoryEntity;
import product.repository.AuctionProductRepository;
import product.repository.ProductionAuctionHistoryRepository;

@Service
public class ProductConsumer {

	@Autowired
	private AuctionProductRepository repository;
	@Autowired
	private ProductionAuctionHistoryRepository pahr;
	
	
	@KafkaListener(topics = "${kafka.topic.request-topic}")
	@SendTo
	public String listen(String request) throws InterruptedException, JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(request, Map.class);
		
		AuctionProductEntity ape = repository.findByProductId(Long.parseLong(map.get("productId").toString()));
		System.out.println(ape);
		if(Long.parseLong(ape.getBidPrice()) >= Long.parseLong(map.get("pay").toString())) {
			return "제시한 가격이 현재 입찰가보다 낮습니다.";
		}
		map.put("bidUser", Long.toString(ape.getBidUser()));
		map.put("bidPrice", ape.getBidPrice());
		ape.setBidPrice(map.get("pay").toString());
		ape.setBidUser(Long.parseLong(map.get("userId").toString()));
		repository.save(ape);

		pahr.save(ProductAuctionHistoryEntity.builder()
											 .productId(ape.getProductId())
											 .bidUser(Long.parseLong(map.get("userId").toString()))
											 .bidPrice(map.get("pay").toString())
											 .build());
		
		return mapper.writeValueAsString(map);
	}
}
