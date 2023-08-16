package f4.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import f4.payment.model.AccountEntity;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long>{
	public AccountEntity findByUserId(long id);
}
