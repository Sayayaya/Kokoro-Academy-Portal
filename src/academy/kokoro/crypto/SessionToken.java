package academy.kokoro.crypto;

public class SessionToken {

    private static final RandomString generator = new RandomString(21);

    /**
     * Gernerates a new Cryptographically secure randomized string that can be used as a session token.
     * @return
     */
    public static String getNewToken()
    {
        return generator.nextString();
    }
}
