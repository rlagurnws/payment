package payment.model;

import java.util.Date;

import javax.persistence.Entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter@Setter
@ToString
@Entity(name = "charge_history")
public class ChargeHistoryEntity {
	
	private long id;
	private long accoungId;
	private long userId;
	private String chargePrice;
	@CreationTimestamp
	private Date applicationDate;
	@UpdateTimestamp
	private Date depositDate;
	private String status;
}
