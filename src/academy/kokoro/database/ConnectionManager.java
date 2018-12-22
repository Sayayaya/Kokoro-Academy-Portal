package academy.kokoro.database;

import academy.kokoro.server.Environment;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {


    static BasicDataSource basicDataSource = null;

    /**
     * Get a Connection to the Kokoro Academy Portal's Database.
     * @return Connection to Database
     * @throws SQLException Could not obtain a connection to database.
     */
    public static Connection getConnection() throws SQLException
    {
        if (basicDataSource == null) {
            String username = Environment.getValue("db_username");
            String password = Environment.getValue("db_password");
            String url = Environment.getValue("db_url");

            basicDataSource = new BasicDataSource();
            basicDataSource.setUsername(username);
            basicDataSource.setPassword(password);
            basicDataSource.setUrl(url);
            basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
            basicDataSource.setDefaultQueryTimeout(8);
            basicDataSource.setDefaultAutoCommit(true);
        }
        return basicDataSource.getConnection();
    }
}
