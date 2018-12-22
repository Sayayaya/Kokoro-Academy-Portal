package academy.kokoro.authentication;

import academy.kokoro.database.ConnectionManager;
import academy.kokoro.portal.people.Person;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import javax.annotation.Priority;
import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


@Authenticated
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String REALM = "portal";
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {
            // There are two supported forms of Authentication. 'Magic' & 'Token'
            // Token can be passed via
            // 1. query parameter
            // 2. HttpHeader
            // 3. or read from a cookie.
            // Magic can be passed via query parameter only.
            // @See: README.md under Authentication.
            Person person;
            // 1. Check the Query Parameters
            if (requestContext.getUriInfo().getQueryParameters().containsKey("Magic")) {
                // A Magic Key was passed for authentication.
                String magic = requestContext.getUriInfo().getQueryParameters().getFirst("Magic");
                person = validateMagic(magic);
                if (person != null) {
                    setSecurityContext(requestContext, person);
                    return; // All good
                }
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            } else if (requestContext.getUriInfo().getQueryParameters().containsKey("Token")) {
                String token = requestContext.getUriInfo().getQueryParameters().getFirst("Token");
                person = validateToken(token);
                if (person != null) {
                    setSecurityContext(requestContext, person);
                    return; // All good
                }
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }
            // 2. Check HttpHeader
            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                // Authorization Header was passed. Lets check this for a token.
                String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
                person = validateToken(token);
                if (person != null) {
                    setSecurityContext(requestContext, person);
                    return; // All good
                }
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }
            // 3. Check Cookies
            if (requestContext.getCookies().containsKey("Kokoro-Portal-Token")) {
                // The cookie contains the token
                String token = requestContext.getCookies().get("Kokoro-Portal-Token").getValue();
                person = validateToken(token);
                if (person != null) {
                    setSecurityContext(requestContext, person);
                    return; // All good
                }
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }
            // Finally, if we've got this far, no authentication method was provided.
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } catch (SQLException e) {
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }
    }

    /**
     * Validates the 'Magic' that has been passed.
     * @param magic the `Magic` sent as query parameter.
     * @return the Person the `Magic` is for, or <em>null</em> if the token is invalid or has already been used.
     * @throws SQLException database comm. error
     */
    private Person validateMagic(String magic) throws SQLException {
        Person person;
        String sql = "SELECT `Person` FROM `Magic_Links` WHERE `Magic` = ?";
        try (Connection connection = ConnectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, magic);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return null;
                person = new Person(resultSet.getLong(1));
            }
        }
        // We have the Person, now delete the Magic Link so it can't be used again.
        String sql2 = "DELETE FROM `Magic_Links` WHERE `Magic` = ?";
        try (Connection connection = ConnectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql2)) {
            statement.setString(1, magic);
            statement.executeUpdate();
        }
        return person;
    }


    /**
     * Validates the token.
     * @param token the token passed either as query parameter or as authorization header, or stored in a cookie in the REST request.
     * @return a Person on success, or null if the token is not recognized.
     * @throws SQLException database comm. error
     */
    private Person validateToken(String token) throws SQLException {
        // Check if the token was issued by the server and if it's not expired
        // Throw an Exception if the token is invalid

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT `Person` FROM `Tokens` WHERE `Token` = ?"))
        {
            statement.setString(1, token);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (!resultSet.next()) return null; // No such Token
                return new Person(resultSet.getLong(1));
            }
        }
    }

    /**
     * Sets the request security Context to the Person.
     * @param requestContext
     * @param person
     */
    private void setSecurityContext(ContainerRequestContext requestContext, Person person) throws SQLException {
        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
        String username = person.getUserName();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> username;
            }

            @Override
            public boolean isUserInRole(String s) {
                return true;
            }

            @Override
            public boolean isSecure() {
                return currentSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return AUTHENTICATION_SCHEME;
            }
        });
    }

}
