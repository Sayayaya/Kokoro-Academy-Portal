package academy.kokoro.portal.people;

import academy.kokoro.database.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;

public class DayAvailability {

    /**
     * The periods of free time the Person has during this particular day.
     */
    final TimeRange[] freeTimes;

    private DayAvailability(TimeRange[] freeTimes)
    {
        this.freeTimes = freeTimes;
    }

    /**
     * Get the persons general day availability for a given day. (Not considering events the user might be signed up to at the school)
     * @param dayOfWeek the day of the week to check availability on
     * @param person the Person to lookup
     * @return the availability of the person on that given day.
     * @throws SQLException database connection issues
     */
    public static DayAvailability getDayAvailability(DayOfWeek dayOfWeek, Person person) throws SQLException
    {
        int day = dayOfWeek.getValue();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `FromHour`, `ToHour` FROM `DayAvailibilities` WHERE `Person` = ? AND `Day` = ?"))
        {
            statement.setLong(1, person.id);
            statement.setInt(2, day);
            try (ResultSet resultSet = statement.executeQuery())
            {
                ArrayList<TimeRange> ranges = new ArrayList<>();
                while (resultSet.next()) {
                    ranges.add(new TimeRange(resultSet.getInt(1), resultSet.getInt(2)));
                }
                return new DayAvailability(ranges.toArray(new TimeRange[ranges.size()]));
            }
        }
    }

    /**
     * Get the Person's general weekly availability (not including events the user is participating in at the school)
     * @param person the Person to lookup
     * @return an array, 7 long, representing the person's daily availability monday - sunday
     * @throws SQLException database connection issues
     */
    public static DayAvailability[] getWeekAvailability(Person person) throws SQLException
    {
        DayAvailability[] dayAvailabilities = new DayAvailability[7];

        for (int i = 0; i < 7; i++)
        {
            dayAvailabilities[i] = getDayAvailability(DayOfWeek.of(i), person);
        }
        return dayAvailabilities;
    }

    /**
     * Returns true if the Person has indicated they are free between these hours on this day.
     * @param startHour time range start
     * @param endHour time range end
     * @return true if the person is free during the hours on this day.
     */
    public Boolean availableBetween(int startHour, int endHour) {
        for (TimeRange freeTime : freeTimes) {
            if (freeTime.inRange(startHour, endHour)) return true;
        }
        return false;
    }
}
