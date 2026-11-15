package org.delcom.ifs25026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortOptionTest {

    @Test
    void sortsByAmountAscendingAndDescending() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(1, 1, "Besar", 500_000L, TransactionType.INCOME));
        transactions.add(new Transaction(2, 1, "Kecil", 100_000L, TransactionType.EXPENSE));

        transactions.sort(SortOption.AMOUNT_ASC.comparator());
        assertEquals(100_000L, transactions.get(0).getAmount());

        transactions.sort(SortOption.AMOUNT_DESC.comparator());
        assertEquals(500_000L, transactions.get(0).getAmount());
    }

    @Test
    void sortsByType() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(1, 1, "Keluar", 100_000L, TransactionType.EXPENSE));
        transactions.add(new Transaction(2, 1, "Masuk", 200_000L, TransactionType.INCOME));

        transactions.sort(SortOption.INCOME_FIRST.comparator());
        assertTrue(transactions.get(0).isIncome());

        transactions.sort(SortOption.EXPENSE_FIRST.comparator());
        assertTrue(!transactions.get(0).isIncome());
    }

    @Test
    void sortsByNewestFirst() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
                1, 1, "Lama", 1L, TransactionType.INCOME, null, LocalDateTime.of(2026, 1, 1, 8, 0)));
        transactions.add(new Transaction(
                2, 1, "Baru", 1L, TransactionType.INCOME, null, LocalDateTime.of(2026, 2, 1, 8, 0)));

        transactions.sort(SortOption.NEWEST_FIRST.comparator());
        assertEquals("Baru", transactions.get(0).getDescription());
    }
}
