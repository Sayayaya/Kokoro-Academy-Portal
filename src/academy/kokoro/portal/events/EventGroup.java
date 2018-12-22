package academy.kokoro.portal.events;

import academy.kokoro.database.ConnectionManager;
import academy.kokoro.portal.people.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * EventGroups are groups of 1 or more events. Every Event belongs to an EventGroup.
 *
 * The purpose of the EventGroup is to allow recurring events such as courses, regular meetings, to be 'grouped'
 *
 * For example, a 'Japanese' Group, might have several EventGroups. One EventGroup might represent a Japanese Course,
 * containing 20 events, whilst another EventGroup might be for a special festival, that only contains one event.
 */
public class EventGroup {

    long id;

    public EventGroup(long id)
    {
        this.id = id;
    }

    /**
     * Get all of the Events belonging to this EventGroup.
     * @return
     * @throws SQLException
     */
    public Event[] getEvents() throws SQLException
    {
        try (   Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT `Event` FROM `Events` WHERE `EventGroup`=?"))
        {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery())
            {
                ArrayList<Event> events = new ArrayList<>();
                while (resultSet.next())
                {
                    events.add(new Event(resultSet.getLong(1)));
                }
                return events.toArray(new Event[events.size()]);
            }
        }
    }

    /**
     * Get the Creator of the EventGroup.
     * @return
     * @throws SQLException
     */
    public Person getCreator() throws SQLException
    {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `Creator` FROM `EventGroups` WHERE `id` = ?"))
        {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    return new Person(resultSet.getLong(1));
                }
                throw new SQLException("No such EventGroup");
            }
        }
    }
}
