package org.delcom.ifs25026springbootcashflow.usecase;

import org.delcom.ifs25026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int USER_A = 1;
    private static final int USER_B = 2;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addAndGetTransactionsByUser() throws ValidationException {
        Transaction income = useCase.addTransaction(USER_A, "Gaji", 5_000_000L, TransactionType.INCOME);
        Transaction expense = useCase.addTransaction(USER_A, "Makan", 25_000L, TransactionType.EXPENSE);

        assertEquals(1, income.getId());
        assertEquals(USER_A, income.getUserId());
        assertEquals(2, useCase.getTransactionsByUser(USER_A).size());
        assertTrue(useCase.getTransactionsByUser(USER_B).isEmpty());
        assertEquals(2, useCase.getAllTransactions().size());
        assertTrue(income.isIncome());
        assertEquals(TransactionType.EXPENSE, expense.getType());
    }

    @Test
    void addTransactionValidation() {
        assertThrows(ValidationException.class,
                () -> useCase.addTransaction(USER_A, " ", 100L, TransactionType.INCOME));
        assertThrows(ValidationException.class,
                () -> useCase.addTransaction(USER_A, null, 100L, TransactionType.INCOME));
        assertThrows(ValidationException.class,
                () -> useCase.addTransaction(USER_A, "Nol", 0L, TransactionType.INCOME));
        assertThrows(ValidationException.class,
                () -> useCase.addTransaction(USER_A, "Minus", -1L, TransactionType.EXPENSE));
        assertThrows(ValidationException.class,
                () -> useCase.addTransaction(USER_A, "Tanpa tipe", 100L, null));
    }

    @Test
    void removeTransactionSuccessAndNotFound() throws Exception {
        Transaction transaction = useCase.addTransaction(USER_A, "Hapus", 100L, TransactionType.EXPENSE);
        useCase.removeTransactionForUser(USER_A, transaction.getId());
        assertTrue(useCase.getTransactionsByUser(USER_A).isEmpty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> useCase.removeTransaction(99)
        );
        assertEquals(99, ex.getId());
    }

    @Test
    void removeTransactionRejectsOtherOwner() throws Exception {
        Transaction transaction = useCase.addTransaction(USER_A, "Milik A", 100L, TransactionType.INCOME);
        assertThrows(ValidationException.class,
                () -> useCase.removeTransactionForUser(USER_B, transaction.getId()));
        assertThrows(EntityNotFoundException.class,
                () -> useCase.removeTransactionForUser(USER_A, 999));
    }

    @Test
    void updateTransactionPartialAndValidation() throws Exception {
        Transaction transaction = useCase.addTransaction(USER_A, "Lama", 1_000L, TransactionType.EXPENSE);

        useCase.updateTransactionForUser(
                USER_A, transaction.getId(), "Baru", 2_000L, TransactionType.INCOME, "proofs/a.png", false);
        Transaction updated = useCase.getTransactionsByUser(USER_A).get(0);
        assertEquals("Baru", updated.getDescription());
        assertEquals(2_000L, updated.getAmount());
        assertEquals(TransactionType.INCOME, updated.getType());
        assertEquals("proofs/a.png", updated.getProofPath());

        useCase.updateTransactionForUser(
                USER_A, transaction.getId(), null, null, null, null, true);
        assertNull(useCase.getTransactionsByUser(USER_A).get(0).getProofPath());

        useCase.updateTransaction(transaction.getId(), "Judul Saja", null, null, null, false);
        assertEquals("Judul Saja", useCase.getTransactionsByUser(USER_A).get(0).getDescription());

        useCase.updateTransaction(transaction.getId(), null, null, null, null, false);
        assertEquals("Judul Saja", useCase.getTransactionsByUser(USER_A).get(0).getDescription());

        assertThrows(ValidationException.class,
                () -> useCase.updateTransaction(transaction.getId(), "  ", null, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransaction(transaction.getId(), null, 0L, null, null, false));
        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransaction(999, "X", null, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(
                        USER_B, transaction.getId(), "X", null, null, null, false));
        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransactionForUser(
                        USER_A, 999, "X", null, null, null, false));
    }

    @Test
    void searchSortTotalsAndBalance() throws Exception {
        useCase.addTransaction(USER_A, "Gaji Kantor", 5_000_000L, TransactionType.INCOME);
        useCase.addTransaction(USER_A, "Bayar Listrik", 500_000L, TransactionType.EXPENSE);
        useCase.addTransaction(USER_A, "Makan Siang", 25_000L, TransactionType.EXPENSE);

        assertEquals(1, useCase.searchTransactions(USER_A, "gaji").size());
        assertEquals(3, useCase.searchTransactions(USER_A, null).size());
        assertEquals(3, useCase.searchTransactions(USER_A, "").size());

        List<Transaction> amountAsc = useCase.sortTransactions(USER_A, SortOption.AMOUNT_ASC);
        assertEquals(25_000L, amountAsc.get(0).getAmount());
        assertEquals(5_000_000L, amountAsc.get(amountAsc.size() - 1).getAmount());
        assertTrue(useCase.sortTransactions(USER_A, SortOption.INCOME_FIRST).get(0).isIncome());

        assertEquals(5_000_000L, useCase.getTotalIncome(USER_A));
        assertEquals(525_000L, useCase.getTotalExpense(USER_A));
        assertEquals(4_475_000L, useCase.getBalance(USER_A));
    }
}
