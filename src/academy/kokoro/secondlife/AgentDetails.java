package academy.kokoro.secondlife;

import java.util.UUID;

public class AgentDetails {

    public final UUID agent;
    public final String username;
    public final String displayName;

    public AgentDetails(UUID agent) {
        this.agent = agent;
        // TODO Communicate with Script URL In-World to retrieve the agent's Username & Displayname
        username = null;
        displayName = null;
    }
}
