package payment.controller;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import payment.model.AccountEntity;
import payment.repository.AccountRepository;

@RestController
public class PaymentController {

	@Autowired
	private ReplyingKafkaTemplate<String, String, String> kafkaTemplate;
	
	@Value("${kafka.topic.request-topic}")
	private String requestTopic;
	
	@Value("${kafka.topic.requestreply-topic}")
	private String requestReplyTopic;
	
	@Autowired
	private AccountRepository repository;
	
	@PostMapping(value="/order",produces=MediaType.APPLICATION_JSON_VALUE,consumes=MediaType.APPLICATION_JSON_VALUE)
	public String pay(@RequestBody Map<String,Object> map) throws InterruptedException, ExecutionException, JsonProcessingException {
		//userId에 맞는 user 계좌 조회
		AccountEntity ae = repository.findByUserId(Long.parseLong(map.get("userId").toString()));
		//잔액 부족할 경우 바로 리턴
		if(Long.parseLong(ae.getBalance()) < Long.parseLong(map.get("pay").toString())) {
			return "잔액 부족";
		}
		
		//map <-> JSON 위한 매퍼 생성
		ObjectMapper mapper = new ObjectMapper();
		String request = mapper.writeValueAsString(map);
		
		//ReplyingKafkaTemplate 생성
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(requestTopic, request);
		record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, requestReplyTopic.getBytes()));
		RequestReplyFuture<String, String, String> sendAndReceive = kafkaTemplate.sendAndReceive(record);
		ConsumerRecord<String, String> consumerRecord = sendAndReceive.get();
		String str = consumerRecord.value();
		
		//입찰가보다 낮을 경우 결과 바로 리턴
		if(str.startsWith("제시")||str.startsWith("연속")) {
			return str;
		}
		
		//받아온 JSON -> Map으로 변환
		Map<String,Object> result = mapper.readValue(str,Map.class);
		System.out.println(result);
		//새롭게 입찰 시도한 user계좌 차감
		ae.setBalance(Long.toString(Long.parseLong(ae.getBalance())-Long.parseLong(result.get("pay").toString())));
		repository.save(ae);
		
		//기존 입찰자 계좌 환불
		AccountEntity pre = repository.findByUserId(Long.parseLong(result.get("bidUser").toString()));
		//기존 입찰자 없는 경우
		if(pre==null) {
			return "입찰 성공~~";
		}
		pre.setBalance(Long.toString(Long.parseLong(pre.getBalance())+Long.parseLong(result.get("bidPrice").toString())));
		repository.save(pre);
		
		return "입찰 성공~";
	}
}
