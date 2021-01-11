/*  This file is part of the Rade project (https://github.com/mgimpel/rade).
 *  Copyright (C) 2018 Marc Gimpel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* $Id$ */
package fr.aesn.rade.persist.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity: Généalogie Entité Administrative.
 * <pre>
 * CREATE TABLE ZR_GENEALOGIE (
 *   PARENT          integer       NOT NULL,
 *   ENFANT          integer       NOT NULL,
 *   TYPE_GENEALOGIE varchar(2)    NOT NULL,
 *   COMMENTAIRE     varchar(2000),
 *   PRIMARY KEY (PARENT, ENFANT),
 *   FOREIGN KEY (PARENT) REFERENCES ZR_ENTITEADMIN,
 *   FOREIGN KEY (ENFANT) REFERENCES ZR_ENTITEADMIN,
 *   FOREIGN KEY (TYPE_GENEALOGIE) REFERENCES ZR_TYPEGENEALOGIE  
 * );
 * </pre>
 * NB: Il est nécessaire de créer la class de liaison parent-enfant car elle
 * contient plus que simplement les liens parents et enfants, mais aussi des
 * données caracteriasant ce lien (type de généalogie et commentaire sur la
 * généalogie)
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Entity
@Table(name = "ZR_GENEALOGIE")
@Getter @Setter @NoArgsConstructor
@ToString @EqualsAndHashCode
public class GenealogieEntiteAdmin implements Serializable {
  /** Unique Identifier for Serializable Class. */
  private static final long serialVersionUID = 6185300766888133445L;

  /**
   * Embeddable Class to be used as a Composite Primary Key for
   * GenealogieEntiteAdmin. It is composed of two elements:
   * <ul>
   * <li>Parent</li>
   * <li>Enfant</li>
   * </ul>
   * @author Marc Gimpel (mgimpel@gmail.com)
   */
  @Embeddable
  @Getter @Setter @NoArgsConstructor
  @ToString @EqualsAndHashCode
  public static class ParentEnfant implements Serializable {
    /** Unique Identifier for Serializable Class. */
    private static final long serialVersionUID = -1832480232514464075L;

    /** Entité Administrative parent de la relation. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARENT", nullable = false)
    private EntiteAdministrative parent;

    /** Entité Administrative enfant de la relation. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ENFANT", nullable = false)
    private EntiteAdministrative enfant;
  }

  /** Composite Primary Key for GenealogieEntiteAdmin. */
  @EmbeddedId
  private ParentEnfant parentEnfant;

  /** Type de Généalogie qui défini la relation. */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "TYPE_GENEALOGIE", nullable = true)
  private TypeGenealogieEntiteAdmin typeGenealogie;

  /** Commentaire sur la relation. */
  @Size(max = 2000)
  @Column(name = "COMMENTAIRE", length = 2000, nullable = true)
  private String commentaire;
}
