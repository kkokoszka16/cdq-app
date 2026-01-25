package com.banking.application.port.in;

import com.banking.application.dto.CategoryStatistics;
import com.banking.application.dto.IbanStatistics;
import com.banking.application.dto.MonthlyStatistics;

import java.time.YearMonth;

/**
 * Input port for retrieving transaction statistics.
 */
public interface GetStatisticsUseCase {

    CategoryStatistics getStatisticsByCategory(YearMonth month);

    IbanStatistics getStatisticsByIban(YearMonth month);

    MonthlyStatistics getStatisticsByMonth(int year);
}
