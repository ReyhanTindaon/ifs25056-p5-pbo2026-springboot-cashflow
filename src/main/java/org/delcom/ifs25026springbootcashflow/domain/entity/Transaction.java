package org.delcom.ifs25026springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entity inti satu transaksi keuangan pribadi.
 * Berada di layer domain, tidak bergantung pada Vaadin, Spring, maupun JDBC.
 */
public class Transaction {
    /** ID unik transaksi. */
    private final int id;

    /** Pemilik transaksi. */
    private final int userId;

    /** Keterangan transaksi. */
    private String description;

    /** Nominal transaksi dalam rupiah (selalu positif). */
    private long amount;

    /** Jenis transaksi: pemasukan atau pengeluaran. */
    private TransactionType type;

    /** Path relatif bukti transaksi (opsional). */
    private String proofPath;

    /** Waktu transaksi dibuat. */
    private final LocalDateTime createdAt;

    public Transaction(int id, int userId, String description, long amount, TransactionType type) {
        this(id, userId, description, amount, type, null, LocalDateTime.now());
    }

    public Transaction(
            int id,
            int userId,
            String description,
            long amount,
            TransactionType type,
            String proofPath,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.description = description == null ? "" : description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    /** Mengubah keterangan transaksi. */
    public void changeDescription(String description) {
        this.description = description == null ? "" : description;
    }

    /** Mengubah nominal transaksi. */
    public void changeAmount(long amount) {
        this.amount = amount;
    }

    /** Mengubah jenis transaksi. */
    public void changeType(TransactionType type) {
        this.type = type;
    }

    /** Mengubah path bukti transaksi. */
    public void changeProofPath(String proofPath) {
        this.proofPath = proofPath;
    }
}
