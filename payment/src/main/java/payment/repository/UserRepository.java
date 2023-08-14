package payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import payment.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{

}
