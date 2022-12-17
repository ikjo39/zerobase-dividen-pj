package com.ikjo39.dividen.persist;

import com.ikjo39.dividen.persist.entity.DividendEntity;
import java.time.LocalDateTime;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {

	List<DividendEntity> findAllByCompanyId(Long companyId);

	@Transactional
	void deleteAllByCompanyId(Long id);

	boolean existsByCompanyIdAndDate(Long companyId, LocalDateTime date);

}
