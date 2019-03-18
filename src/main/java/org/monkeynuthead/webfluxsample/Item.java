package org.monkeynuthead.webfluxsample;

public final class Item {

    private final String id;
    private final String description;

    private Item(final String id, final String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    static Item of(final String id, final String description) {
        return new Item(id, description);
    }

}
