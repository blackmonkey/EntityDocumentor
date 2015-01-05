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

public class TreeVariableBackup {
    protected static final String KEY_ID = "id";
    protected static final String KEY_CNNAME = "cnname";
    protected static final String KEY_ENNAME = "enname";
    protected static final String KEY_DESC = "desc";
    protected static final String KEY_PARENT = "parent";

    protected static final char KV_SEP = ':';

    public static final int INVALID_ID = Integer.MIN_VALUE;

    private byte[] initChecksum;

    private int id = INVALID_ID;
    private int parentId = INVALID_ID;
    private String cnName = "";
    private String enName = "";
    private String descrip = "";
    private TreeVariableBackup parent;
    private Vector<TreeVariableBackup> children = new Vector<TreeVariableBackup>();

    protected String getSnapshot() {
        StringBuffer buf = new StringBuffer();
        buf.append(cnName);
        buf.append(enName);
        buf.append(descrip);
        buf.append(getIdPath());

        for (TreeVariableBackup child : children) {
            buf.append(child.getSnapshot());
        }

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

    public void onRemove() {
        int pos = -1;

        // remove self from parent children
        if (parent != null) {
            pos = parent.children.indexOf(this);
            parent.children.remove(this);
        }

        // attach children to parent at old position
        for (TreeVariableBackup child : children) {
            child.parent = parent;
            child.parentId = parentId;
        }
        if (parent != null && pos != -1) {
            parent.children.addAll(pos, children);
        }
    }

    public boolean isRoot() {
        return parentId == INVALID_ID || parent == null;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TreeVariableBackup) {
            return Arrays.equals(getChecksum(), ((TreeVariableBackup)obj).getChecksum());
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

    public TreeVariableBackup getParent() {
        return parent;
    }

    public void setParent(TreeVariableBackup p) {
        if (parent != null) {
            parent.children.remove(this);
        }
        parent = p;
        parentId = p.id;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public Vector<TreeVariableBackup> getChildren() {
        return children;
    }

    private static String getPath(Vector<String> path) {
        StringBuffer buf = new StringBuffer();
        for (int i = path.size() - 1; i >= 0; i--) {
            buf.append(path.get(i));
            buf.append('>');
        }
        if (buf.length() > 1) {
            buf.setLength(buf.length() - 1); // remove trailing '>'
        }
        return buf.toString();
    }

    /**
     * Get inherit path in variable ids.
     *
     * @return inherit path
     */
    protected String getIdPath() {
        Vector<String> path = new Vector<String>();
        TreeVariableBackup var = this;
        while (var != null) {
            path.add(String.valueOf(var.id));
            var = var.parent;
        }
        return getPath(path);
    }

    /**
     * Get inherit path in variable names.
     *
     * @param english {@code true} to get English path, {@code false} to get Chinese path.
     * @return inherit path
     */
    public String getNamePath(boolean english) {
        Vector<String> path = new Vector<String>();
        TreeVariableBackup var = this;
        while (var != null) {
            path.add(english ? var.enName : var.cnName);
            var = var.parent;
        }
        return getPath(path);
    }

    protected static int parseIdString(String val) {
        if (TextUtils.isEmpty(val)) {
            return INVALID_ID;
        }

        // find potential non-number character
        int pos = 0;
        for (; pos < val.length(); pos++) {
            char c = val.charAt(pos);
            if (c < '0' || c > '9') {
                break;
            }
        }
        if (pos < val.length()) {
            val = val.substring(0, pos);
        }

        return Integer.parseInt(val);
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
                    id = parseIdString(val);
                } else if (key.equals(KEY_PARENT)) {
                    parentId = parseIdString(val);
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
        return String.format("%s(%s)", cnName, enName);
    }
}
