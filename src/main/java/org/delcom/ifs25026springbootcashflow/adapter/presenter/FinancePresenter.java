package org.delcom.ifs25026springbootcashflow.adapter.presenter;

import org.delcom.ifs25026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026springbootcashflow.domain.entity.TransactionType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat transaksi menjadi teks siap tampil.
 * Format uang berada di sini, bukan di entity atau use case.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Memformat nominal menjadi rupiah dengan pemisah ribuan dan dua desimal. */
    public String formatAmount(long amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return "Rp " + format.format(amount);
    }

    /** Memformat satu transaksi menjadi baris teks. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatAmount(transaction.getAmount()),
                typeLabel(transaction.getType())
        );
    }

    /** Label jenis transaksi dalam bahasa Indonesia. */
    public String typeLabel(TransactionType type) {
        if (type == TransactionType.INCOME) {
            return "Pemasukan";
        }
        return "Pengeluaran";
    }

    /** Memformat tanggal transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat saldo. */
    public String formatBalance(long balance) {
        return "Saldo: " + formatAmount(balance);
    }

    /** Memformat daftar transaksi multi-baris. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah transaksi: " + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi.";
    }

    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    public String showInvalidAmount() {
        return "[!] Jumlah tidak valid!";
    }

    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
