package academy.kokoro.server;

public class Environment {

    /**
     * Get a Server Environment Variable. Used for retrieving sensitive server-only passwords and the like.
     * This will be programmed to use Amazon Tags for the time being.
     * @param key - The Identifier to find
     * @return value - The value
     */
    public static String getValue(String key)
    {
        return null; // TODO
    }

}
