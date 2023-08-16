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
	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;
	
	@KafkaListener(topics = "${kafka.topic.request-topic}")
	@SendTo
	public String listen(String request) throws InterruptedException, JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(request, Map.class);

		ProductEntity pe = service.getPE(map.get("productId").toString());

		//입찰가에 대한 유효성 검사
		AuctionProductEntity ape = service.getAP(map.get("productId").toString());
		if(Long.parseLong(map.get("pay").toString())==Long.parseLong(pe.getPrice())) {
			map = service.immediately(map);
		}else
		if(Long.parseLong(ape.getBidPrice()) >= Long.parseLong(map.get("pay").toString())) {
			map.put("status", 0);
			map.put("result", "제시한 가격이 현재 입찰가보다 낮습니다.");
		}else if(ape.getBidUser()==Long.parseLong(map.get("userId").toString())) {
			map.put("status", 0);
			map.put("result", "연속으로 입찰할 수 없습니다.");
		}else if(Long.parseLong(pe.getPrice()) <= Long.parseLong(map.get("pay").toString())){
			map.put("status", 0);
			map.put("result", "즉시 구매를 하세요;;");
		}else {
			map.put("status", 1);
			map.put("result", "입찰 성공하셨습니다.");
		}
		
		//입찰 불가 시 결과값 return 가능하면 마무리
		if(Integer.parseInt(map.get("status").toString())==0) {
			return mapper.writeValueAsString(map);
		}
		map = service.done(map, ape);
		
		//email service 발행
		Map<String,Object> dataForEmail = new HashMap<>();
//		dataForEmail.put("")
		kafkaTemplate.send("email","test");
		return mapper.writeValueAsString(map);
	}
	
	@KafkaListener(topics = "email")
	public void emailTest(String str) {
		System.out.println("**********Email Listener 실행 됐다");
		System.out.println(str);
	}
}
