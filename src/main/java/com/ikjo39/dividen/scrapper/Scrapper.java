package com.ikjo39.dividen.scrapper;

import com.ikjo39.dividen.model.Company;
import com.ikjo39.dividen.model.ScrapedResult;

public interface Scrapper {
	Company scrapCompanyByTicker(String ticker);
	ScrapedResult scrap(Company company);

}
