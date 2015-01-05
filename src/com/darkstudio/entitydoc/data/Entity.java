package com.darkstudio.entitydoc.data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import com.darkstudio.entitydoc.utils.TextUtils;

public class Entity extends ESItem {
    private static final String KEY_ATTRS = "attributes";

    private static final String ATTR_ID_SEP = ",";

    private LinkedHashSet<Integer> attrIds = new LinkedHashSet<Integer>();

    public void setAttributes(Vector<ESItem> attrs) {
        attrIds.clear();
        for (ESItem attr : attrs) {
            attrIds.add(attr.getId());
        }
    }

    public Vector<Integer> getAttributeIds() {
        return new Vector<Integer>(attrIds);
    }

    private void parseAttributesIds(String val) {
        String[] ids = val.split(ATTR_ID_SEP);
        for (String id : ids) {
            int aid = TextUtils.parseInt(id, INVALID_ID);
            if (aid != INVALID_ID) {
                attrIds.add(aid);
            }
        }
    }

    private String getAttributeIds(boolean trimComma) {
        StringBuffer buf = new StringBuffer();
        for (Integer attrId : attrIds) {
            buf.append(attrId);
            buf.append(ATTR_ID_SEP);
        }
        if (trimComma && buf.length() > 1) {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    @Override
    protected String getSnapshot() {
        return getAttributeIds(false) + super.getSnapshot();
    }

    @Override
    protected Vector<String> parseRawData(List<String> lines) {
        Vector<String> unrec = super.parseRawData(lines);
        for (int i = 0; i < unrec.size(); i++) {
            String l = unrec.get(i);
            int pos = l.indexOf(KV_SEP);
            if (pos != -1) {
                String key = l.substring(0, pos);
                String val = l.substring(pos + 1).trim();
                if (key.equals(KEY_ATTRS)) {
                    parseAttributesIds(val);
                    unrec.remove(i);
                    break; // skip potential remaining lines
                }
            }
        }
        markUnchanged();
        return unrec;
    }

    @Override
    public List<String> getRawData() {
        List<String> lines = super.getRawData();
        writeMember(lines, KEY_ATTRS, getAttributeIds(true));
        return lines;
    }
}
