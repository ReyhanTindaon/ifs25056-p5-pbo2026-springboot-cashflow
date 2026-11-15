package org.delcom.ifs25026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void constructorDefaults() {
        Transaction transaction = new Transaction(1, 10, "Gaji", 5_000_000L, TransactionType.INCOME);

        assertEquals(1, transaction.getId());
        assertEquals(10, transaction.getUserId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(5_000_000L, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertNull(transaction.getProofPath());
        assertNotNull(transaction.getCreatedAt());
        assertTrue(transaction.isIncome());
        assertFalse(new Transaction(2, 10, "Belanja", 100L, TransactionType.EXPENSE).isIncome());
    }

    @Test
    void nullDescriptionBecomesEmpty() {
        Transaction transaction = new Transaction(
                1,
                1,
                null,
                1_000L,
                TransactionType.EXPENSE,
                null,
                LocalDateTime.of(2026, 11, 1, 8, 0)
        );
        assertEquals("", transaction.getDescription());
    }

    @Test
    void mutatorsUpdateFields() {
        Transaction transaction = new Transaction(1, 1, "Lama", 1_000L, TransactionType.EXPENSE);
        transaction.changeDescription("Baru");
        transaction.changeAmount(2_500L);
        transaction.changeType(TransactionType.INCOME);
        transaction.changeProofPath("proofs/a.png");

        assertEquals("Baru", transaction.getDescription());
        assertEquals(2_500L, transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals("proofs/a.png", transaction.getProofPath());
        assertTrue(transaction.isIncome());

        transaction.changeDescription(null);
        assertEquals("", transaction.getDescription());
        transaction.changeProofPath(null);
        assertNull(transaction.getProofPath());
    }
}
