package fr.aesn.rade.persist.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ZR_ARRONDISSEMENT")
@Getter @Setter @NoArgsConstructor
@ToString(callSuper = true)
//@EqualsAndHashCode(callSuper = true)
public class Arrondissement implements Serializable {
	 /** Unique Identifier for Serializable Class. */
	  private static final long serialVersionUID = 704275233608860782L;

	  /** Code INSEE de l arrondissement. */
	  @Id
	  @Size(max = 5)
	  @Column(name = "CODE_ARRONDISSEMENT", length = 10, nullable = false)
	  private String codeInsee;

	  /** id code commune. */ 	  
	  @Size(max = 5)
	  @Column(name = "CODE_COMMUNE", length = 5, nullable = false)
	  private Integer id;

}
