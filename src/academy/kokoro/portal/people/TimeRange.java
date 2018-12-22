package academy.kokoro.portal.people;

class TimeRange
{
    final int hourStart;
    final int hourEnd;

    public Boolean inRange(int startHour, int endHour)
    {
        if (startHour <= 0) throw new IllegalArgumentException("Invalid Start Hour: " + startHour);
        if (startHour >= hourStart && endHour <= hourEnd) return true;
        return false;
    }

    public TimeRange(int hourStart, int hourEnd)
    {
        if ( hourStart > hourEnd || hourStart <= 0 || hourStart >= 24 ||
                hourEnd <= 1 || hourEnd > 24) throw new IllegalArgumentException("Invalid Range: " + hourStart + " - " + hourEnd);
        this.hourStart = hourStart;
        this.hourEnd = hourEnd;

    }
}
