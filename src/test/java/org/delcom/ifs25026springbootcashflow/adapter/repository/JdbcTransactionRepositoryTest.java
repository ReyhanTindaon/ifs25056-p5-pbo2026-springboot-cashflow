package org.delcom.ifs25026springbootcashflow.adapter.repository;

import org.delcom.ifs25026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026springbootcashflow.domain.entity.TransactionType;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTransactionRepositoryTest {

    private JdbcTransactionRepository repository;
    private int userId;

    @BeforeEach
    void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:tx-" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        SchemaInitializer.initialize(dataSource);
        JdbcUserRepository users = new JdbcUserRepository(dataSource);
        userId = users.saveNew("pemilik", "hash", "Pemilik", Role.USER).getId();
        repository = new JdbcTransactionRepository(dataSource);
    }

    @Test
    void savesReadsFiltersAndDeletes() {
        Transaction income = repository.save(userId, "Gaji", 5_000_000L, TransactionType.INCOME);
        Transaction expense = repository.save(userId, "Makan", 25_000L, TransactionType.EXPENSE);

        assertEquals(2, repository.findAll().size());
        assertEquals(2, repository.findByUserId(userId).size());
        assertTrue(repository.findByUserId(userId + 1).isEmpty());
        assertEquals(1, repository.findBy(transaction -> transaction.isIncome()).size());
        assertEquals("Gaji", repository.findById(income.getId()).orElseThrow().getDescription());
        assertTrue(repository.findById(9_999).isEmpty());
        assertNull(repository.findById(income.getId()).orElseThrow().getProofPath());

        assertTrue(repository.deleteById(expense.getId()));
        assertFalse(repository.deleteById(expense.getId()));
        assertTrue(repository.findAll().stream().noneMatch(t -> t.getId() == expense.getId()));
    }

    @Test
    void saveEntityDelegatesAndUpdatesProofPath() {
        Transaction manual = new Transaction(0, userId, "Manual", 500L, TransactionType.INCOME);
        Transaction created = repository.save(manual);
        assertTrue(created.getId() > 0);

        created.changeProofPath("proofs/bukti.png");
        repository.update(created);
        assertEquals("proofs/bukti.png", repository.findById(created.getId()).orElseThrow().getProofPath());

        created.changeProofPath(null);
        repository.update(created);
        assertNull(repository.findById(created.getId()).orElseThrow().getProofPath());
    }

    @Test
    void wrapsSqlFailuresForEveryQuery() throws Exception {
        JdbcTransactionRepository broken = new JdbcTransactionRepository(failingDataSource());
        Transaction existing = new Transaction(1, userId, "X", 1L, TransactionType.INCOME);

        assertThrows(IllegalStateException.class, broken::findAll);
        assertThrows(IllegalStateException.class, () -> broken.findByUserId(userId));
        assertThrows(IllegalStateException.class, () -> broken.findById(1));
        assertThrows(IllegalStateException.class,
                () -> broken.save(userId, "X", 1L, TransactionType.INCOME));
        assertThrows(IllegalStateException.class, () -> broken.deleteById(1));
        assertThrows(IllegalStateException.class, () -> broken.update(existing));
        assertThrows(IllegalStateException.class, () -> broken.findBy(transaction -> true));
    }

    @Test
    void saveFailsWhenGeneratedKeyIsMissing() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(statement);
        when(statement.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);

        JdbcTransactionRepository broken =
                new JdbcTransactionRepository(dataSourceReturning(connection));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> broken.save(userId, "Tanpa ID", 1L, TransactionType.EXPENSE));
        assertEquals("Gagal mendapatkan ID transaksi baru", ex.getMessage());
    }

    @Test
    void findByIdIgnoresFailureWhenClosingResultSet() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        doThrow(new SQLException("close gagal")).when(resultSet).close();

        JdbcTransactionRepository repo =
                new JdbcTransactionRepository(dataSourceReturning(connection));

        Optional<Transaction> found = repo.findById(1);
        assertTrue(found.isEmpty());
    }

    @Test
    void findAllReturnsDefensiveList() {
        repository.save(userId, "A", 1L, TransactionType.INCOME);
        List<Transaction> all = repository.findAll();
        all.clear();
        assertEquals(1, repository.findAll().size());
    }

    private static DataSource failingDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("koneksi putus"));
        return dataSource;
    }

    private static DataSource dataSourceReturning(Connection connection) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        return dataSource;
    }
}
