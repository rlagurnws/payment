package f4.payment.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long accountId;
    private long userId;
    private String useBalance;
    private String status;
    @CreationTimestamp
    private Date useDate;
    private String type;
}
