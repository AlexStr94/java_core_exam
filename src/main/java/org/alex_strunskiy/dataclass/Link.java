package org.alex_strunskiy.dataclass;

import java.sql.Timestamp;
import java.util.UUID;

public class Link {
    UUID uuid;
    int limit;
    Timestamp created;
    String longLink;

    public Link(UUID uuid, int limit, Timestamp created, String longLink) {
        this.uuid = uuid;
        this.limit = limit;
        this.created = created;
        this.longLink = longLink;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLimit() {
        return limit;
    }

    public Timestamp getCreated() {
        return created;
    }

    public String getLongLink() {
        return longLink;
    }
}
