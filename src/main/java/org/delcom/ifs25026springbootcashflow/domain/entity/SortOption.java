package org.delcom.ifs25026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi. Setiap opsi membawa comparator-nya sendiri
 * sehingga logika pengurutan terpusat di domain.
 */
public enum SortOption {
    /** Nominal dari kecil ke besar. */
    AMOUNT_ASC(Comparator.comparingLong(Transaction::getAmount)),

    /** Nominal dari besar ke kecil. */
    AMOUNT_DESC(Comparator.comparingLong(Transaction::getAmount).reversed()),

    /** Pemasukan tampil lebih dulu. */
    INCOME_FIRST(Comparator.comparing(Transaction::isIncome).reversed()),

    /** Pengeluaran tampil lebih dulu. */
    EXPENSE_FIRST(Comparator.comparing(Transaction::isIncome)),

    /** Waktu terbaru tampil lebih dulu. */
    NEWEST_FIRST(Comparator.comparing(Transaction::getCreatedAt).reversed());

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
