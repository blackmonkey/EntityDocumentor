package com.darkstudio.entitydoc.data;

import java.util.Vector;

import com.darkstudio.entitydoc.Messages;

public class AttributeManager extends ESItemManager {

    private Vector<String> types = new Vector<String>();

    public AttributeManager() {
        super(Messages.get("AttributeMgr.Title"));
        initBuiltinTypes();
    }

    private void initBuiltinTypes() {
        types.add("Bool (1 bit)");
        types.add("Integer (8 bits)");
        types.add("Integer (16 bits)");
        types.add("Integer (32 bits)");
        types.add("Integer (64 bits)");
        types.add("Integer[] (8 bits)");
        types.add("Integer[] (16 bits)");
        types.add("Integer[] (32 bits)");
        types.add("Integer[] (64 bits)");
        types.add("Double (32 bits)");
        types.add("Double (64 bits)");
        types.add("Double[] (32 bits)");
        types.add("Double[] (64 bits)");
        types.add("UTF8 String");
    }

    @Override
    protected String getStorageFilename() {
        return "Attributes";
    }

    /**
     * Create a new {@link ESItem} instance.
     *
     * @return {@link ESItem} instance
     */
    @Override
    protected Attribute createNewVariable() {
        return new Attribute();
    }

    public Vector<String> getDataTypes() {
        return types;
    }

    private void updateTypes(ESItem item) {
        if (item instanceof Attribute) {
            String type = ((Attribute) item).getDataType();
            if (!types.contains(type)) {
                types.add(type);
            }
        }
    }

    @Override
    protected void onParsedItem(ESItem item) {
        updateTypes(item);
    }

    /**
     * Notified after the specified {@link ESItem} is updated.
     *
     * @param item the updated {@link ESItem}.
     */
    public void onUpdateItem(ESItem item) {
        updateTypes(item);
    }
}
