package f4.product.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String name;
	private String category;
	private String images;
	private String artist;
	private String description;
	private String size;
	private String completionDate;
	private String status;
	private String technique;
	private String style;
	private String theme;
	private String country;
	private String price;
}
