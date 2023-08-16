package f4.payment.service;

import java.util.Map;

import f4.payment.model.UserEntity;
import f4.payment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import f4.payment.model.AccountEntity;
import f4.payment.repository.AccountRepository;

@Service
public class BidService {

	@Autowired
	private AccountRepository repository;
	@Autowired
	private UserRepository userRepository;

	public AccountEntity getAE(Map<String, Object> map) {
		return repository.findByUserId(Long.parseLong(map.get("userId").toString()));
	}
	
	public String done(Map<String,Object> result, AccountEntity ae) {
		ae.setBalance(Long.toString(Long.parseLong(ae.getBalance()) - Long.parseLong(result.get("pay").toString())));
		repository.save(ae);

		AccountEntity pre = repository.findByUserId(Long.parseLong(result.get("bidUser").toString()));
		if (pre == null) {
			return result.get("result").toString();
		}
		pre.setBalance(Long.toString(Long.parseLong(pre.getBalance()) + Long.parseLong(result.get("bidPrice").toString())));
		repository.save(pre);
		
		//사용자 예치금 내역 추가하는 로직 필요
		
		return result.get("result").toString();
	}

	public UserEntity getUE(String id){
		return userRepository.findById(Long.parseLong(id)).get();
	}
}
