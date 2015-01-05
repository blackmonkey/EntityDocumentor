package com.darkstudio.entitydoc.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.zip.CRC32;

import com.darkstudio.entitydoc.utils.TextUtils;

public class ESItem {
    protected static final String KEY_ID = "id";
    protected static final String KEY_CNNAME = "cnname";
    protected static final String KEY_ENNAME = "enname";
    protected static final String KEY_DESC = "desc";
    protected static final String KEY_PARENT = "parent";

    protected static final char KV_SEP = ':';

    public static final int INVALID_ID = -1;

    private byte[] initChecksum;

    private int id = INVALID_ID;
    private int parentId = INVALID_ID;
    private String cnName = "";
    private String enName = "";
    private String descrip = "";

    protected String getSnapshot() {
        StringBuffer buf = new StringBuffer();
        buf.append('|');
        buf.append(id);
        buf.append(',');
        buf.append(parentId);
        buf.append(',');
        buf.append(cnName);
        buf.append(',');
        buf.append(enName);
        buf.append(',');
        buf.append(descrip);
        buf.append('|');
        return buf.toString();
    }

    private static byte[] getChecksum(String snapshot) {
        byte[] snapshotBytes = null;
        try {
            snapshotBytes = snapshot.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            snapshotBytes = snapshot.getBytes();
        }

        try {
            return MessageDigest.getInstance("MD5").digest(snapshotBytes);
        } catch (NoSuchAlgorithmException e) {
        }

        try {
            return MessageDigest.getInstance("SHA-1").digest(snapshotBytes);
        } catch (NoSuchAlgorithmException e) {
        }

        CRC32 crc = new CRC32();
        crc.update(snapshotBytes);
        long checksum = crc.getValue();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(checksum);
        return buffer.array();
    }

    private byte[] getChecksum() {
        return getChecksum(getSnapshot());
    }

    public void markUnchanged() {
        initChecksum = getChecksum();
    }

    public boolean isChanged() {
        byte[] curChecksum = getChecksum();
        return !Arrays.equals(initChecksum, curChecksum);
    }

    public boolean hasParent() {
        return parentId != INVALID_ID;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ESItem) {
            return Arrays.equals(getChecksum(), ((ESItem)obj).getChecksum());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getChecksum());
    }

    public int getId() {
        return id;
    }

    public void setId(int v) {
        id = v;
    }

    public String getChineseName() {
        return cnName;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int v) {
        parentId = v;
    }

    public void setChineseName(String name) {
        cnName = name;
    }

    public String getEnglishName() {
        return enName;
    }

    public void setEnglishName(String name) {
        enName = name;
    }

    public String getDescription() {
        return descrip;
    }

    public void setDescription(String desc) {
        descrip = desc;
    }

    /**
     * Parse loaded data to initialize.
     *
     * @param lines raw data of attribute/entity.
     * @return unrecognized lines
     */
    protected Vector<String> parseRawData(List<String> lines) {
        Vector<String> unrec = new Vector<String>();
        for (String l : lines) {
            int pos = l.indexOf(KV_SEP);
            if (pos == -1) {
                unrec.add(l);
            } else {
                String key = l.substring(0, pos);
                String val = l.substring(pos + 1).trim();
                if (key.equals(KEY_ID)) {
                    id = TextUtils.parseInt(val, INVALID_ID);
                } else if (key.equals(KEY_PARENT)) {
                    parentId = TextUtils.parseInt(val, INVALID_ID);
                } else if (key.equals(KEY_CNNAME)) {
                    cnName = TextUtils.unescape(val);
                } else if (key.equals(KEY_ENNAME)) {
                    enName = TextUtils.unescape(val);
                } else if (key.equals(KEY_DESC)) {
                    descrip = TextUtils.unescape(val);
                } else {
                    unrec.add(l);
                }
            }
        }
        markUnchanged();
        return unrec;
    }

    protected static List<String> writeMember(List<String> lines, String key, String val) {
        lines.add(key + KV_SEP + TextUtils.escape(val));
        return lines;
    }

    /**
     * Generate data of attribute to save on storage.
     *
     * @return the raw data of attribute.
     */
    public List<String> getRawData() {
        List<String> lines = new Vector<String>();
        writeMember(lines, KEY_ID, id == INVALID_ID ? "" : String.valueOf(id));
        writeMember(lines, KEY_PARENT, parentId == INVALID_ID ? "" : String.valueOf(parentId));
        writeMember(lines, KEY_CNNAME, cnName);
        writeMember(lines, KEY_ENNAME, enName);
        writeMember(lines, KEY_DESC, descrip);
        return lines;
    }

    @Override
    public String toString() {
        return String.format("%s(%s) %s", cnName, enName, isChanged() ? "*" : "");
    }

    /**
     * Get the HTML string to shown in {@link javax.swing.JList} cell.
     *
     * @param bgcolor background color of HTML
     * @return HTML string.
     */
    public String getCellHtml(String bgcolor) {
        return String.format("<html><div style='background:%s;font:normal 10pt Verdana'><b>%s(%s)</b><br/>%s</div></html>",
                             bgcolor, cnName, enName, TextUtils.getPlainText(descrip, 20, false));
    }
}
