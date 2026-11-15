package org.delcom.ifs25026springbootcashflow.adapter.presenter;

import org.delcom.ifs25026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {

    private FinancePresenter presenter;

    @BeforeEach
    void setUp() {
        presenter = new FinancePresenter();
    }

    private Transaction transaction() {
        return new Transaction(
                1,
                1,
                "Gaji bulanan",
                1_000_000L,
                TransactionType.INCOME,
                null,
                LocalDateTime.of(2026, 11, 1, 8, 15)
        );
    }

    @Test
    void formatsAmountBalanceAndDate() {
        assertEquals("Rp 1,000,000.00", presenter.formatAmount(1_000_000L));
        assertEquals("Rp 0.00", presenter.formatAmount(0L));
        assertEquals("Saldo: Rp -25,000.00", presenter.formatBalance(-25_000L));
        assertEquals("01/11/2026 08:15", presenter.formatCreatedAt(transaction()));
    }

    @Test
    void formatsTransactionAndTypeLabels() {
        assertEquals("Pemasukan", presenter.typeLabel(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.typeLabel(TransactionType.EXPENSE));
        assertEquals(
                "[1] Gaji bulanan | Rp 1,000,000.00 | [Pemasukan]",
                presenter.format(transaction())
        );
        Transaction expense = new Transaction(2, 1, "Makan", 25_000L, TransactionType.EXPENSE);
        assertTrue(presenter.format(expense).contains("[Pengeluaran]"));
    }

    @Test
    void formatsListEmptyAndFilled() {
        assertEquals("Header:\n- kosong", presenter.formatList(List.of(), "Header:", "- kosong"));
        String filled = presenter.formatList(List.of(transaction()), "Header:", "- kosong");
        assertTrue(filled.contains("[1] Gaji bulanan"));
    }

    @Test
    void showMessages() {
        Transaction transaction = transaction();
        assertTrue(presenter.showTransactions(List.of()).contains("belum tersedia"));
        assertTrue(presenter.showTransactions(List.of(transaction)).contains("Gaji bulanan"));
        assertTrue(presenter.showSearchResults(List.of(), "gaji").contains("tidak ditemukan"));
        assertTrue(presenter.showSearchResults(List.of(transaction), "Gaji").contains("Hasil Pencarian"));
        assertTrue(presenter.showSortedTransactions(List.of(transaction)).contains("Terurut"));
        assertTrue(presenter.showAddSuccess(transaction).contains("Berhasil menambah"));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 9.", presenter.showRemoveFailed(9));
        assertEquals("Berhasil mengubah transaksi.", presenter.showUpdateSuccess());
        assertEquals("[!] Gagal mengubah transaksi dengan ID: 8.", presenter.showUpdateFailed(8));
        assertEquals("[!] Jumlah tidak valid!", presenter.showInvalidAmount());
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] keterangan kosong", presenter.showValidationError("keterangan kosong"));
    }
}
