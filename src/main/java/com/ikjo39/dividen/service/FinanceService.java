package com.ikjo39.dividen.service;

import static com.ikjo39.dividen.model.constants.CacheKey.KEY_FINANCE;

import com.ikjo39.dividen.exception.impl.NoCompanyException;
import com.ikjo39.dividen.model.Company;
import com.ikjo39.dividen.model.Dividend;
import com.ikjo39.dividen.model.ScrapedResult;
import com.ikjo39.dividen.persist.CompanyRepository;
import com.ikjo39.dividen.persist.DividendRepository;
import com.ikjo39.dividen.persist.entity.CompanyEntity;
import com.ikjo39.dividen.persist.entity.DividendEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	@Cacheable(key = "#companyName", value = KEY_FINANCE)
	public ScrapedResult getDividendByCompanyName(String companyName) {
		log.info("search Company" + companyName);
		// 1. 회사명을 기준으로 회사 정보 조회
		CompanyEntity company = this.companyRepository.findByName(companyName)
			.orElseThrow(() -> new NoCompanyException());

		// 2. 조회된 회사 ID로 배당금 정보 조회
		List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(
			company.getId());

		// 3. 결과 조합후 반환
//		List<Dividend> dividends = new ArrayList<>();
//		for (var entity : dividendEntities) {
//			dividends.add(Dividend.builder()
//				.date(entity.getDate())
//				.dividend(entity.getDividend())
//				.build());
//		}

		List<Dividend> dividends = dividendEntities.stream().map(e -> new Dividend(e.getDate(), e.getDividend()))
			.collect(Collectors.toList());

		return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends);
	}
}
