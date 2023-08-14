package payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import payment.model.AccountEntity;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long>{

}
