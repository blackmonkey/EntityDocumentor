package com.darkstudio.entitydoc.data;

import java.util.List;
import java.util.Vector;

import com.darkstudio.entitydoc.utils.TextUtils;

public class Attribute extends ESItem {
    private static final String KEY_DATATYPE = "datatype";

    private String dataType = "";

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String type) {
        dataType = type;
    }

    @Override
    protected String getSnapshot() {
        return dataType + super.getSnapshot();
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
                if (key.equals(KEY_DATATYPE)) {
                    dataType = TextUtils.unescape(val);
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
        writeMember(lines, KEY_DATATYPE, dataType);
        return lines;
    }
}
