package payment.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import payment.model.AccountEntity;

@Repository
public interface AccountRepository extends CrudRepository<AccountEntity, Long>{
	public AccountEntity findByUserId(long id);
}
