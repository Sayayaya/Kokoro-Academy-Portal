package academy.kokoro.authentication;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An authenticated user is just a user who is logged in with a session token.
 * Note that, an <em>authenticated</em> user is not necessarily an <em>authorized</em> user.
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Authenticated { }
