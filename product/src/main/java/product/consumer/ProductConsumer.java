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
		//producer에서 보낸 JSON Map으로 변환
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(request, Map.class);
		
		//productId에 맞는 Auction 찾은 후 입찰가와 현재 가격 비교
		AuctionProductEntity ape = repository.findByProductId(Long.parseLong(map.get("productId").toString()));
		if(Long.parseLong(ape.getBidPrice()) >= Long.parseLong(map.get("pay").toString())) {
			return "제시한 가격이 현재 입찰가보다 낮습니다.";
		}
		if(ape.getBidUser()==Long.parseLong(map.get("userId").toString())) {
			return "연속으로 입찰할 수 없습니다.";
		}
		//기존 user와 price 반환하기 위해 map에 put
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
		
		return mapper.writeValueAsString(map);
	}
}
