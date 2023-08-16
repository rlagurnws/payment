package f4.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import f4.payment.model.AccountEntity;
import f4.payment.model.UserEntity;
import f4.payment.service.BidService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
public class PaymentController {

	@Autowired
	private ReplyingKafkaTemplate<String, String, String> kafkaTemplate;

	@Value("${kafka.topic.request-topic}")
	private String requestTopic;

	@Value("${kafka.topic.requestreply-topic}")
	private String requestReplyTopic;

	@Autowired
	private BidService service;

	//userId, productId, pay
	@PostMapping(value = "/bid", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String bid(@RequestBody Map<String, Object> map) throws InterruptedException, ExecutionException, JsonProcessingException {
		AccountEntity ae = service.getAE(map.get("userId").toString());
		UserEntity ue = service.getUE(map.get("userId").toString());
		//결제 비밀번호 확인 로직 필요


		if (Long.parseLong(ae.getBalance()) < Long.parseLong(map.get("pay").toString())) {
			return "잔액 부족";
		}

		map.put("email",ue.getEmail());
		map.put("userName",ue.getName());

		ObjectMapper mapper = new ObjectMapper();
		String request = mapper.writeValueAsString(map);

		// ReplyingKafkaTemplate 생성
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(requestTopic, request);
		record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, requestReplyTopic.getBytes()));
		RequestReplyFuture<String, String, String> sendAndReceive = kafkaTemplate.sendAndReceive(record);
		ConsumerRecord<String, String> consumerRecord = sendAndReceive.get();
		String str = consumerRecord.value();

		// 받아온 JSON -> Map으로 변환
		Map<String, Object> result = mapper.readValue(str, Map.class);
		//입찰 실패 시 결과 반환
		if(Integer.parseInt(result.get("status").toString())==0) {
			return result.get("result").toString();
		}
		
		// 마무~리
		return service.done(result, ae);
	}
}
