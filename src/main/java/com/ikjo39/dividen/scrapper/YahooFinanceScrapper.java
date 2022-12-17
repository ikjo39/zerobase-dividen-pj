package com.ikjo39.dividen.scrapper;

import com.ikjo39.dividen.model.Company;
import com.ikjo39.dividen.model.Dividend;
import com.ikjo39.dividen.model.ScrapedResult;
import com.ikjo39.dividen.model.constants.Month;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component // Bean으로 쓸것이기 때문
public class YahooFinanceScrapper implements Scrapper {

	private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
	private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
	private static final long START_TIME = 86400; // 60 * 60 * 24

	@Override
	public ScrapedResult scrap(Company company) {

		var scrapedResult = new ScrapedResult();
		scrapedResult.setCompany(company);

		try {
			long now = System.currentTimeMillis() / 1000; // 현재 시간 초단위


			String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);

			// http Connection -> parsing->Document를 Jsoup이 해줌
			Connection connection = Jsoup.connect(
				url);
			Document document = connection.get();
//			테이블 태그가 historical-prices class 속성이 있는걸 볼 수 있음
			Elements parsingDivs = document.getElementsByAttributeValue("data-test",
				"historical-prices");
			Element tableEle = parsingDivs.get(0);

			Element tbody = tableEle.children().get(1);
			List<Dividend> dividends = new ArrayList<>();
			for (Element e : tbody.children()) {
				// 배당금 데이터는 전부 dividend 로 끝남
				String txt = e.text();
				if (!txt.endsWith("Dividend")) {
					continue;
				}

				String[] splits = txt.split(" ");
				int month = Month.strToNumber(splits[0]);
				int day = Integer.parseInt(splits[1].replace(",", ""));
				int year = Integer.parseInt(splits[2]);
				String dividend = splits[3];

				if (month < 0) {
					throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
				}

				dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

//				System.out.println(year + "/" + month + "/" + day + " -> " + dividend);
			}
			scrapedResult.setDividends(dividends);

		} catch (IOException e) {
			//TODO
			e.printStackTrace();
		}
		return scrapedResult;
	}

	// 훨씬 더 간단함 ticker로 해당하는 회사 메타정보를 스크랩 후 결과
	@Override
	public Company scrapCompanyByTicker(String ticker) {
		String url = String.format(SUMMARY_URL, ticker, ticker);

		try {
			Document document = Jsoup.connect(url).get();
			Element titleEle = document.getElementsByTag("h1").get(0);
			// 데이터 특성에 따라 이렇게 함
			String title = titleEle.text().split(" - ")[1].trim();

			return new Company(ticker, title);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
