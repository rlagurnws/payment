package payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import payment.model.ChargeHistoryEntity;

@Repository
public interface ChargeRepository extends JpaRepository<ChargeHistoryEntity, Long>{

}
