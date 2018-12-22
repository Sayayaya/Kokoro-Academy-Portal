package academy.kokoro.portal.groups;

import academy.kokoro.database.ConnectionManager;
import academy.kokoro.portal.people.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
public class Group {

    public final long id;

    public Group(long id)
    {
        this.id = id;
    }

    public String getName() throws SQLException
    {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `Name` FROM `Groups` WHERE `ID` = ?"))
        {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    return resultSet.getString(1);
                }
                throw new SQLException("No such group");
            }
        }
    }

    public Person  getOwner() throws SQLException
    {
        try (Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT `Owner` FROM `Groups` WHERE `ID` = ?"))
        {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    return new Person(resultSet.getLong(1));
                }
                throw new SQLException("No such group");
            }
        }
    }

    public Person[] getMembers() throws SQLException
    {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `Member` FROM `GroupMembers` WHERE `Group` = ?"))
        {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery())
            {
                ArrayList<Person> people = new ArrayList<>();
                while (resultSet.next())
                {
                    people.add(new Person(resultSet.getLong(1)));
                }

                return people.toArray(new Person[people.size()]);
            }
        }
    }

}
