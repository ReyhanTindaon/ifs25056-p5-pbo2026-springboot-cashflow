package org.delcom.ifs25026springbootcashflow.adapter.repository;

import org.delcom.ifs25026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveAssignsIncrementalIds() {
        Transaction first = repository.save(1, "Gaji", 5_000_000L, TransactionType.INCOME);
        Transaction second = repository.save(1, "Makan", 25_000L, TransactionType.EXPENSE);

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(2, repository.findAll().size());
        assertEquals(2, repository.findByUserId(1).size());
        assertTrue(repository.findByUserId(2).isEmpty());
    }

    @Test
    void findByIdAndDelete() {
        Transaction transaction = repository.save(1, "Hapus", 100L, TransactionType.EXPENSE);

        Optional<Transaction> found = repository.findById(transaction.getId());
        assertTrue(found.isPresent());
        assertEquals("Hapus", found.get().getDescription());

        assertTrue(repository.deleteById(transaction.getId()));
        assertFalse(repository.deleteById(transaction.getId()));
        assertTrue(repository.findById(transaction.getId()).isEmpty());
    }

    @Test
    void findByPredicateAndUpdateNoOp() {
        repository.save(1, "Gaji", 10_000L, TransactionType.INCOME);
        repository.save(1, "Bonus", 5_000L, TransactionType.INCOME);

        List<Transaction> bonuses = repository.findBy(
                transaction -> transaction.getDescription().contains("Bonus"));
        assertEquals(1, bonuses.size());

        Transaction transaction = bonuses.get(0);
        transaction.changeDescription("Bonus Tahunan");
        repository.update(transaction);

        assertEquals("Bonus Tahunan", repository.findById(transaction.getId()).orElseThrow().getDescription());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.save(1, "A", 1L, TransactionType.INCOME);
        List<Transaction> all = repository.findAll();
        all.clear();
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void saveEntityDirectly() {
        Transaction entity = new Transaction(99, 1, "Manual", 500L, TransactionType.INCOME);
        repository.save(entity);
        assertTrue(repository.findById(99).isPresent());
    }
}
