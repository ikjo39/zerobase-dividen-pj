package com.ikjo39.dividen.scheduler;

import static com.ikjo39.dividen.model.constants.CacheKey.KEY_FINANCE;

import com.ikjo39.dividen.model.Company;
import com.ikjo39.dividen.model.ScrapedResult;
import com.ikjo39.dividen.persist.CompanyRepository;
import com.ikjo39.dividen.persist.DividendRepository;
import com.ikjo39.dividen.persist.entity.CompanyEntity;
import com.ikjo39.dividen.persist.entity.DividendEntity;
import com.ikjo39.dividen.scrapper.Scrapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScrapperScheduler {

	private final CompanyRepository companyRepository;
	private final DividendRepository dividendRepository;
	private final Scrapper yahooFinanceScrapper;


//	@Scheduled(fixedDelay = 1000)
//	public void test1() throws InterruptedException {
//		Thread.sleep(10000);	// 10초간 일시 정지
//		System.out.println(Thread.currentThread().getName() + "테스트 1: " + LocalDateTime.now());
//	}
//
//	@Scheduled(fixedDelay = 1000)
//	public void test2() {
//		System.out.println(Thread.currentThread().getName() +"테스트 2: " + LocalDateTime.now());
//	}


	// 일정 주기마다 수행
	@CacheEvict(value = KEY_FINANCE, allEntries = true)
	@Scheduled(cron = "${scheduler.scrap.yahoo}")
	public void yahooFinanceScheduling() {
		log.info("scrapping scheduler is started");
		// 저장된 회사 목록을 조회

		List<CompanyEntity> companyEntities = this.companyRepository.findAll();
		// 회사마다 배당금 정보를 새로 스크래핑
		for (var company : companyEntities) {
			log.info("scrapping scheduler is started -> " + company.getName());
			ScrapedResult scrapedResult = this.yahooFinanceScrapper.scrap(new Company(company.getName(), company.getTicker()));
			// 스크래핑한 배당금 정보 중 DB에 없는 값은 저장

			scrapedResult.getDividends().stream()
				// Dividend 모델을 DividendEntity 로 매핑함
				.map(e -> new DividendEntity(company.getId(), e))
				// 엘리 먼트를 하나씩 Dividend Repository 에 삽입
				.forEach(e -> {
					boolean exists = this.dividendRepository.existsByCompanyIdAndDate(
						e.getCompanyId(),
						e.getDate());
					if (!exists) {
						this.dividendRepository.save(e);
						log.info("insert new dividend -> " + e.toString());
					}
				});

		}

		// 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

	}

}
