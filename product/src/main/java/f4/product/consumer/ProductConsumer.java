package f4.product.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import f4.product.model.AuctionProductEntity;
import f4.product.model.ProductEntity;
import f4.product.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductConsumer {

	@Autowired
	private BidService service;
	
	@KafkaListener(topics = "${kafka.topic.request-topic}")
	@SendTo
	public String listen(String request) throws InterruptedException, JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(request, Map.class);

		ProductEntity pe = service.getPE(map.get("productId").toString());

		//입찰가에 대한 유효성 검사
		AuctionProductEntity ape = service.getAP(map.get("productId").toString());
		if (Long.parseLong(map.get("pay").toString()) == Long.parseLong(pe.getPrice())) {
			map = service.immediately(map);
		} else if (Long.parseLong(ape.getBidPrice()) >= Long.parseLong(map.get("pay").toString())) {
			map.put("status", 0);
			map.put("result", "제시한 가격이 현재 입찰가보다 낮습니다.");
		} else if (ape.getBidUser() == Long.parseLong(map.get("userId").toString())) {
			map.put("status", 0);
			map.put("result", "연속으로 입찰할 수 없습니다.");
		} else if (Long.parseLong(pe.getPrice()) <= Long.parseLong(map.get("pay").toString())) {
			map.put("status", 0);
			map.put("result", "즉시 구매를 하세요;;");
		} else {
			map.put("status", 1);
			map.put("result", "입찰 성공하셨습니다.");
		}

		//입찰 불가 시 결과값 return 가능하면 마무리
		if (Integer.parseInt(map.get("status").toString()) == 0) {
			return mapper.writeValueAsString(map);
		}
		map.put("productName", pe.getName());
		map.put("image", pe.getImages().split(" ")[0]);
		map.put("bidUser", Long.toString(ape.getBidUser()));
		map.put("bidPrice", ape.getBidPrice());

		return mapper.writeValueAsString(map);
	}


	@KafkaListener(topics = "transaction")
	@SendTo
	public String resultTransaction(String request) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(request,Map.class);

		AuctionProductEntity ape = service.getAP(map.get("productId").toString());

		if(service.done(map, ape)){
			return "true";
		}else{
			return "false";
		}
	}
		//email service 발행
//		Map<String,Object> dataForEmail = new HashMap<>();
//		kafkaTemplate.send("email","test");
}
