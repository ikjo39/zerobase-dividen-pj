package com.ikjo39.dividen.service;

import com.ikjo39.dividen.exception.impl.NoCompanyException;
import com.ikjo39.dividen.model.Company;
import com.ikjo39.dividen.model.ScrapedResult;
import com.ikjo39.dividen.persist.CompanyRepository;
import com.ikjo39.dividen.persist.DividendRepository;
import com.ikjo39.dividen.persist.entity.CompanyEntity;
import com.ikjo39.dividen.persist.entity.DividendEntity;
import com.ikjo39.dividen.scrapper.Scrapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class CompanyService {

	private final Trie trie;

	private final Scrapper yahooFinanceScrapper;
	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;

	public Company save(String ticker) {
		// 이미 저장된 회사인지 한번 더 추가
		boolean exists = this.companyRepository.existsByTicker(ticker);
		if (exists) {
			throw new RuntimeException("already exists ticker -> " + ticker);
		}
		return this.storeCompanyAndDividend(ticker);
		// 우리가 db에 저장되지 않은 회사인 경우에만 아래 머세드 실행함
	}

	public Page<CompanyEntity> getAllCompany(Pageable pageable) {
		return this.companyRepository.findAll(pageable);
	}

	private Company storeCompanyAndDividend(String ticker) {
		// ticker 를 기준으로 회사를 스크래핑
		Company company = this.yahooFinanceScrapper.scrapCompanyByTicker(ticker);
		if (ObjectUtils.isEmpty(company)) {
			throw new RuntimeException("failed to scrap ticker -> " + ticker);

		}
		// 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
		ScrapedResult scrapedResult = this.yahooFinanceScrapper.scrap(company);

		// 스크래핑 결과
		CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
		List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
			.map(e -> new DividendEntity(companyEntity.getId(), e)).collect(
				Collectors.toList());
		dividendRepository.saveAll(dividendEntities);
		return company;
	}

	public List<String> getCompanyNameByKeyword(String keyword) {

		// Pageable 활용
		Pageable limit = PageRequest.of(0, 10);

		Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(
			keyword, limit);
		return companyEntities.stream().map(e -> e.getName()).collect(Collectors.toList());
	}

	public void addAutoCompleteKeyword(String keyword) {
		this.trie.put(keyword, null);
	}

	public List<String> autoComplete(String keyword) {
		return (List<String>) this.trie.prefixMap(keyword).keySet().stream()
			.limit(10) // 서비스의 완성도 높이기
			.collect(Collectors.toList());
	}

	public void deleteAutoCompleteKeyword(String keyword) {
		this.trie.remove(keyword);
	}

	public String deleteCompany(String ticker) {
		var company = this.companyRepository.findByTicker(ticker)
			.orElseThrow(() -> new NoCompanyException());

		this.dividendRepository.deleteAllByCompanyId(company.getId());
		this.companyRepository.delete(company);

		// 자동완성 -> trie에서도 지워져야함
		this.deleteAutoCompleteKeyword(company.getName());
		return company.getName();
	}
}
