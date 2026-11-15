package org.delcom.ifs25026springbootcashflow.adapter.repository;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchemaInitializerTest {

    @Test
    void wrapsSqlExceptionWithActionableMessage() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("koneksi ditolak"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> SchemaInitializer.initialize(dataSource));

        assertTrue(ex.getMessage().startsWith("Gagal menginisialisasi schema database"));
        assertTrue(ex.getMessage().contains("DB_HOST"));
    }

    @Test
    void utilityClassCannotBeInstantiatedExternally() throws Exception {
        Constructor<SchemaInitializer> constructor = SchemaInitializer.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertTrue(constructor.newInstance() instanceof SchemaInitializer);
    }
}
