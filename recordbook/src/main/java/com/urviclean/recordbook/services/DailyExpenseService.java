package com.urviclean.recordbook.services;

import com.urviclean.recordbook.exception.InvalidInputException;
import com.urviclean.recordbook.models.DailyExpenseRecord;
import com.urviclean.recordbook.models.DailySummaryResponse;
import com.urviclean.recordbook.repositories.DailyExpenseRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service for managing day-level expenses per salesman.
 *
 * <p>Expense is stored once per (salesman_alias, date) in {@code daily_expense_record}.
 * After saving or updating an expense, the daily_summary for that salesman/date is
 * automatically recalculated to reflect the change.</p>
 */
@Service
public class DailyExpenseService {

    @Autowired
    private DailyExpenseRecordRepository dailyExpenseRecordRepository;

    @Autowired
    private DailySummaryService dailySummaryService;

    /**
     * Create or update the total expense for a salesman on a given date,
     * then recalculate and persist the daily_summary for that salesman/date.
     *
     * @param salesmanAlias the salesman alias
     * @param expenseDate   the date of the expense
     * @param totalExpense  total day-level expense amount (null is treated as 0)
     * @return the updated {@link DailySummaryResponse} reflecting the new expense
     * @throws InvalidInputException if salesmanAlias is blank, expenseDate is null,
     *                               or totalExpense is negative
     */
    @Transactional
    public DailySummaryResponse saveOrUpdateExpenseAndRecalculate(
            String salesmanAlias,
            LocalDate expenseDate,
            BigDecimal totalExpense) {

        if (salesmanAlias == null || salesmanAlias.isBlank()) {
            throw new InvalidInputException("salesmanAlias", "must not be blank");
        }
        if (expenseDate == null) {
            throw new InvalidInputException("expenseDate", "must not be null");
        }
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }
        if (totalExpense.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("totalExpense", "must not be negative");
        }

        String alias = salesmanAlias.trim();

        // Upsert daily_expense_record
        DailyExpenseRecord expense = dailyExpenseRecordRepository
                .findBySalesmanAliasAndExpenseDate(alias, expenseDate)
                .orElse(new DailyExpenseRecord(alias, expenseDate, totalExpense));
        expense.setTotalExpense(totalExpense);
        dailyExpenseRecordRepository.save(expense);

        // Recalculate summary, which reads expense from daily_expense_record
        return dailySummaryService.computeAndPersistSummary(alias, expenseDate);
    }
}
