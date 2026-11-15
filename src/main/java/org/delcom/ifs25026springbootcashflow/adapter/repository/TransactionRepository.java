package org.delcom.ifs25026springbootcashflow.adapter.repository;

import org.delcom.ifs25026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Implementasi in-memory transaksi untuk unit test.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type) {
        Transaction transaction = new Transaction(nextId(), userId, description, amount, type);
        return save(transaction);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(transaction -> transaction.getUserId() == userId);
    }

    private int nextId() {
        return ++idCounter;
    }
}
