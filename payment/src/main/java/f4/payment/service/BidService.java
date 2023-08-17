package f4.payment.service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import f4.payment.model.TransactionHistory;
import f4.payment.model.UserEntity;
import f4.payment.repository.TransactionHistoryRepository;
import f4.payment.repository.UserRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import f4.payment.model.AccountEntity;
import f4.payment.repository.AccountRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BidService {

	@Autowired
	private AccountRepository repository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TransactionHistoryRepository thRepository;
	@Autowired
	private ReplyingKafkaTemplate<String, String, String> kafkaTemplate;
	@Autowired
	private ReplyingKafkaTemplate<String, String, Boolean> transactionKafkaTemplate;

	public AccountEntity getAE(String userId) {
		return repository.findByUserId(Long.parseLong(userId));
	}

	//ReplyingKafka 생성 메소드
	public String makeReplyingKafka(String requestTopic, String replyTopic, String request) throws ExecutionException, InterruptedException {
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(requestTopic, request);
		record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes()));
		RequestReplyFuture<String, String, String> sendAndReceive = kafkaTemplate.sendAndReceive(record);
		ConsumerRecord<String, String> consumerRecord = sendAndReceive.get();
		return consumerRecord.value();
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean done(Map<String,Object> result, AccountEntity ae, String request) throws Exception{
		ae.setBalance(Long.toString(Long.parseLong(ae.getBalance()) - Long.parseLong(result.get("pay").toString())));
		repository.save(ae);

		AccountEntity pre = repository.findByUserId(Long.parseLong(result.get("bidUser").toString()));
		if (pre != null) {
			pre.setBalance(Long.toString(Long.parseLong(pre.getBalance()) + Long.parseLong(result.get("bidPrice").toString())));
			repository.save(pre);
		}
		//사용자 예치금 내역 추가
		thRepository.save(TransactionHistory.builder().accountId(ae.getId())
					.userId(ae.getUserId()).useBalance(result.get("pay").toString()).status("bid")
					.type("bid").build());

		//트랜잭션 이벤트 발행
		ProducerRecord<String, String> record = new ProducerRecord<String, String>("transaction", request);
		record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "transactionresult".getBytes()));
		RequestReplyFuture<String, String, Boolean> sendAndReceive = transactionKafkaTemplate.sendAndReceive(record);
		ConsumerRecord<String, Boolean> consumerRecord = sendAndReceive.get();
		boolean transactionResult = consumerRecord.value();
		if(transactionResult){
			return true;
		}else{
			throw new Exception();
		}
	}

	public UserEntity getUE(String id){
		return userRepository.findById(Long.parseLong(id)).get();
	}
}
