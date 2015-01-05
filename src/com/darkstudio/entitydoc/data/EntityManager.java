package com.darkstudio.entitydoc.data;

import com.darkstudio.entitydoc.Messages;

public class EntityManager extends ESItemManager {

    public EntityManager() {
        super(Messages.get("EntityMgr.Title"));
    }

    @Override
    protected String getStorageFilename() {
        return "Entities";
    }

    /**
     * Create a new {@link ESItem} instance.
     *
     * @return {@link ESItem} instance
     */
    @Override
    protected Entity createNewVariable() {
        return new Entity();
    }
}
