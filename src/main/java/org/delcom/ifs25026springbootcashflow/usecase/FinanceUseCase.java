package org.delcom.ifs25026springbootcashflow.usecase;

import org.delcom.ifs25026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Optional;

/**
 * Use case pengelolaan transaksi keuangan per pengguna.
 * Tidak melakukan I/O UI dan tidak bergantung pada Vaadin.
 */
public class FinanceUseCase {
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi milik satu pengguna. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Mengambil semua transaksi (dipakai admin untuk peninjauan). */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan transaksi baru setelah validasi. */
    public Transaction addTransaction(
            int userId,
            String description,
            long amount,
            TransactionType type
    ) throws ValidationException {
        validateDescription(description);
        validateAmount(amount);
        validateType(type);
        return transactionRepository.save(userId, description.trim(), amount, type);
    }

    /** Menghapus transaksi berdasarkan ID. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!transactionRepository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Menghapus transaksi milik pengguna tertentu. */
    public void removeTransactionForUser(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        removeTransaction(transaction.getId());
    }

    /** Mengubah transaksi (tanpa cek pemilik; dipakai layer admin/internal). */
    public void updateTransaction(
            int id,
            String description,
            Long amount,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Optional<Transaction> found = transactionRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException(id);
        }
        Transaction transaction = found.get();
        applyUpdates(transaction, description, amount, type, proofPath, clearProof);
        transactionRepository.update(transaction);
    }

    /** Mengubah transaksi milik pengguna tertentu. */
    public void updateTransactionForUser(
            int userId,
            int id,
            String description,
            Long amount,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        applyUpdates(transaction, description, amount, type, proofPath, clearProof);
        transactionRepository.update(transaction);
    }

    /** Mencari transaksi pengguna yang keterangannya mengandung kata kunci. */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        String lowerKeyword = keyword == null ? "" : keyword.toLowerCase();
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi pengguna sesuai kriteria yang dipilih. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return transactionRepository.findByUserId(userId).stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total pemasukan milik pengguna. */
    public long getTotalIncome(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(Transaction::isIncome)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Total pengeluaran milik pengguna. */
    public long getTotalExpense(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> !transaction.isIncome())
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Saldo pengguna: pemasukan dikurangi pengeluaran. */
    public long getBalance(int userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    private Transaction requireOwnedTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Optional<Transaction> found = transactionRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException(id);
        }
        Transaction transaction = found.get();
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak milik pengguna ini");
        }
        return transaction;
    }

    private void applyUpdates(
            Transaction transaction,
            String description,
            Long amount,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws ValidationException {
        if (description != null) {
            validateDescription(description);
            transaction.changeDescription(description.trim());
        }
        if (amount != null) {
            validateAmount(amount);
            transaction.changeAmount(amount);
        }
        if (type != null) {
            validateType(type);
            transaction.changeType(type);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath);
        }
    }

    private void validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
    }

    private void validateAmount(long amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari nol");
        }
    }

    private void validateType(TransactionType type) throws ValidationException {
        if (type == null) {
            throw new ValidationException("Jenis transaksi harus dipilih");
        }
    }
}
