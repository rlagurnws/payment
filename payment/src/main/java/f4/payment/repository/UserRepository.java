package f4.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import f4.payment.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{

}
