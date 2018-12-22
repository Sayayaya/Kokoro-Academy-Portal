package academy.kokoro.portal.people;

import academy.kokoro.crypto.SessionToken;
import academy.kokoro.database.ConnectionManager;
import academy.kokoro.secondlife.AgentDetails;

import java.sql.*;
import java.util.TimeZone;
import java.util.UUID;

public class Person {

    public final long id;


    public Person(long id)
    {
        this.id = id;
    }

    /**
     * @return The Person's SL Display Name or null if not found.
     * @throws SQLException
     */
    public String getDisplayName() throws SQLException
    {
        try (Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT `DisplayName` FROM `People` WHERE `ID` = ?"))
        {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (!resultSet.next()) return null;
                return resultSet.getString(1 );
            }
        }
    }

    private static Person create(AgentDetails agentDetails) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO `People` (`UUID`,`DisplayName`,`UserName`) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, agentDetails.agent.toString());
            statement.setString(2, agentDetails.displayName);
            statement.setString(3, agentDetails.username);

            if (statement.executeUpdate() != 1) throw new SQLException("Failed to create Person - 0 rows affected");

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                resultSet.next();
                return new Person(resultSet.getLong(1));
            }
        }
    }

    /**
     * Gets Person by their AgentUUID.
     * @param agentUUID The Agent's SecondLife UUID.
     * @return Person or null if the person does not exist.
     * @throws SQLException db communication error
     */
    private static Person get(UUID agentUUID) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `ID` FROM `People` WHERE `UUID` = ? "))
        {
            statement.setString(1, agentUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return new Person(resultSet.getLong(1));
                return null;
            }
        }
    }

    /**
     * Update the Person's Display Name
     * @param displayName
     * @throws SQLException
     */
    public void setDisplayName(String displayName) throws SQLException
    {
        try (Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE `People` SET `display_name` = ? WHERE `ID` = ?"))
        {
            statement.setString(1,displayName);
            statement.setLong(2,id);
            statement.executeUpdate();
        }
    }

    /**
     * Gets the timezone that the user has indicated they belong to, or null if the user has not yet set their timezone.
     * @return
     * @throws SQLException
     */
    public TimeZone getTimeZone() throws SQLException
    {
        try (Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT `timezone` FROM `People` WHERE `id` = ?"))
        {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (!resultSet.next()) return null;
                return TimeZone.getTimeZone(resultSet.getString(1));
            }
        }
    }

    public void setTimeZone(TimeZone timeZone) throws SQLException
    {
        try (Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement("Update `People` SET `timezone` = ? WHERE `id`=?"))
        {
            statement.setString(1,timeZone.getID());
            statement.setLong(2 ,id);
            statement.executeUpdate();
        }
    }

    public String getUserName() throws SQLException
    {
        String sql = "SELECT `UserName` FROM `People` WHERE `ID` = ?";
        try (Connection connection = ConnectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new SQLException("Error - Couldn't find Username. 0 rows?");
                return resultSet.getString(1);
            }
        }
    }

    /**
     * Issues a new Session Token that can be used to authenticate this person.
     * @param agentDetails The details of the SecondLife agent this token will be for.
     * @return a new session token
     */
    public static String getNewToken(AgentDetails agentDetails) throws SQLException
    {
        Person person = Person.get(agentDetails.agent);
        if (person == null) {
            person = Person.create(agentDetails);
        }

        String token = SessionToken.getNewToken();
        try (Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `Tokens` (`Token`,`Person`) VALUES (?,?)"))
        {
            statement.setString(1, token);
            statement.setLong(2, person.id);
            if (statement.executeUpdate() == 1) return token;
            throw new SQLException("Failed to create new Session Token - Rows affected not 1.");
        }
    }

}
