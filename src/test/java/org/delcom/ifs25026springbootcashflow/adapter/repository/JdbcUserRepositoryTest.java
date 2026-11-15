package org.delcom.ifs25026springbootcashflow.adapter.repository;

import org.delcom.ifs25026springbootcashflow.domain.entity.Role;
import org.delcom.ifs25026springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

class JdbcUserRepositoryTest {

    private JdbcUserRepository repository;

    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:user-" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        SchemaInitializer.initialize(dataSource);
        repository = new JdbcUserRepository(dataSource);
    }

    @Test
    void savesReadsFiltersUpdatesAndDeletes() {
        assertTrue(repository.findAll().isEmpty());

        User user = repository.saveNew("andi", "hash", "Andi", Role.USER);
        assertTrue(user.getId() > 0);
        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findByUsername("ANDI").isPresent());
        assertTrue(repository.findByUsername("tidak-ada").isEmpty());
        assertTrue(repository.findById(user.getId()).isPresent());
        assertTrue(repository.findById(9_999).isEmpty());
        assertEquals(1, repository.findBy(candidate -> candidate.isAdmin() == false).size());

        user.changeFullName("Andi Baru");
        user.changePhotoPath("profiles/andi.png");
        repository.update(user);
        User reloaded = repository.findById(user.getId()).orElseThrow();
        assertEquals("Andi Baru", reloaded.getFullName());
        assertEquals("profiles/andi.png", reloaded.getPhotoPath());

        user.changePhotoPath(null);
        repository.update(user);
        assertNull(repository.findById(user.getId()).orElseThrow().getPhotoPath());

        assertTrue(repository.deleteById(user.getId()));
        assertFalse(repository.deleteById(user.getId()));
        assertTrue(repository.findByUsername("andi").isEmpty());
    }

    @Test
    void saveEntityDelegatesAndAssignsId() {
        User created = repository.save(new User(0, "budi", "hash", "Budi", Role.ADMIN, null));
        assertTrue(created.getId() > 0);
        assertEquals(Role.ADMIN, repository.findById(created.getId()).orElseThrow().getRole());
    }

    @Test
    void wrapsSqlFailuresForEveryQuery() throws Exception {
        JdbcUserRepository broken = new JdbcUserRepository(failingDataSource());
        User existing = new User(1, "x", "hash", "X", Role.USER, null);

        assertThrows(IllegalStateException.class, broken::findAll);
        assertThrows(IllegalStateException.class, () -> broken.findById(1));
        assertThrows(IllegalStateException.class, () -> broken.findByUsername("x"));
        assertThrows(IllegalStateException.class,
                () -> broken.saveNew("x", "hash", "X", Role.USER));
        assertThrows(IllegalStateException.class, () -> broken.deleteById(1));
        assertThrows(IllegalStateException.class, () -> broken.update(existing));
        assertThrows(IllegalStateException.class, () -> broken.findBy(user -> true));
    }

    @Test
    void saveNewFailsWhenGeneratedKeyIsMissing() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(statement);
        when(statement.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);

        JdbcUserRepository broken = new JdbcUserRepository(dataSourceReturning(connection));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> broken.saveNew("x", "hash", "X", Role.USER));
        assertEquals("Gagal mendapatkan ID pengguna baru", ex.getMessage());
    }

    @Test
    void queryOptionalIgnoresFailureWhenClosingResultSet() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        doThrow(new SQLException("close gagal")).when(resultSet).close();

        JdbcUserRepository broken = new JdbcUserRepository(dataSourceReturning(connection));
        assertTrue(broken.findById(1).isEmpty());
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
