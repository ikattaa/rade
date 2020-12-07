package fr.aesn.rade.persist.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.aesn.rade.persist.model.Commune;
import fr.aesn.rade.persist.model.EntiteAdministrative;
import fr.aesn.rade.persist.model.TypeEntiteAdmin;

public interface EntiteAdministrativeJpaDao extends JpaRepository
<EntiteAdministrative, Integer> {
	@Query(	" SELECT e from EntiteAdministrative e  WHERE e.id IN("
			+ " SELECT c.id from Commune c WHERE c.codeInsee=:codeInsee OR c.id="
			+ " (SELECT co.id from Commune co"
			+ " JOIN Arrondissement a ON co.id=a.id"
			+ " WHERE a.codeInsee=:codeInsee)"
			+ ")"
			+ "ORDER BY  e.debutValidite DESC"
			)
	public List<EntiteAdministrative> getAllEntiteAdminByCode(@Param("codeInsee") String codeInsee);
}
