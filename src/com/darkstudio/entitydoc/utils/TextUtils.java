package com.darkstudio.entitydoc.utils;

import java.io.IOException;
import java.io.StringReader;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class TextUtils {

    public static final String CELL_NORMAL_BG = "#FFFFFF";
    public static final String CELL_HIGHLIGHT_BG = "#F0F0F0";

    /**
     * <p>
     * <code>TextUtils</code> instances should NOT be constructed in standard programming.
     * </p>
     * <p>
     * Instead, the class should be used as:
     * </p>
     *
     * <pre>
     * TextUtils.escape(&quot;foo&quot;);
     * </pre>
     * <p>
     * This constructor is public to permit tools that require a JavaBean instance to operate.
     * </p>
     */
    public TextUtils() {
    }

    /**
     * <p>
     * Returns an upper case hexadecimal <code>String</code> for the given character.
     * </p>
     *
     * @param ch The character to convert.
     * @return An upper case hexadecimal <code>String</code>
     */
    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }

    public static boolean isEmpty(String txt) {
        return txt == null || txt.length() == 0;
    }

    /**
     * <p>
     * Escapes the characters in a <code>String</code>.
     * </p>
     * <p>
     * Deals correctly with control-chars (tab, backslash, cr, ff, etc.)
     * </p>
     * <p>
     * So a tab becomes the characters <code>'\\'</code> and <code>'t'</code>.
     * </p>
     *
     * @param str String to escape values in, may be null
     * @return String with escaped values, <code>null</code> if null string input
     */
    public static String escape(String str) {
        if (str == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(str.length() * 2);
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            switch (ch) {
            case '\b':
                buf.append("\\b");
                break;
            case '\n':
                buf.append("\\n");
                break;
            case '\t':
                buf.append("\\t");
                break;
            case '\f':
                buf.append("\\f");
                break;
            case '\r':
                buf.append("\\r");
                break;
            default:
                if (ch >= 32) {
                    buf.append(ch);
                } else if (ch > 15) {
                    buf.append("\\x" + hex(ch));
                } else { // less than or equal to 0x0F
                    buf.append("\\x0" + hex(ch));
                }
                break;
            }
        }
        return buf.toString();
    }

    /**
     * <p>
     * Unescapes any Java literals found in the <code>String</code>. For example, it will turn a
     * sequence of <code>'\'</code> and <code>'n'</code> into a newline character, unless the
     * <code>'\'</code> is preceded by another <code>'\'</code>.
     * </p>
     *
     * @param str the <code>String</code> to unescape, may be null
     * @return a new unescaped <code>String</code>, <code>null</code> if null string input
     */
    public static String unescape(String str) {
        if (str == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(str.length());
        StringBuffer hexcode = new StringBuffer(2);
        boolean hadSlash = false;
        boolean inHex = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (inHex) {
                // if in hex code, then we're reading hex code values in somehow
                hexcode.append(ch);
                if (hexcode.length() == 4) {
                    // hex code now contains the two hex digits which represents our hex code
                    // character
                    try {
                        int value = Integer.parseInt(hexcode.toString(), 16);
                        buf.append((char) value);
                    } catch (NumberFormatException nfe) {
                        // Unable to parse hex code, let's output it anyway.
                        buf.append("\\x");
                        buf.append(hexcode);
                    }
                    hexcode.setLength(0);
                    inHex = false;
                    hadSlash = false;
                }
            } else if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
                case 'b':
                    buf.append('\b');
                    break;
                case 'n':
                    buf.append('\n');
                    break;
                case 't':
                    buf.append('\t');
                    break;
                case 'f':
                    buf.append('\f');
                    break;
                case 'r':
                    buf.append('\r');
                    break;
                case 'x':
                    // uh-oh, we're in hex code country....
                    inHex = true;
                    break;
                default:
                    buf.append("\\" + ch);
                    break;
                }
            } else if (ch == '\\') {
                hadSlash = true;
            } else {
                buf.append(ch);
            }
        }
        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the string, let's output it anyway.
            buf.append('\\');
        }
        return buf.toString();
    }

    public static String escapeHtml(String txt) {
        return txt.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static int parseInt(String val, int defVal) {
        if (val != null) {
            val = val.trim();
        }
        if (TextUtils.isEmpty(val)) {
            return defVal;
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

        return TextUtils.isEmpty(val) ? defVal : Integer.parseInt(val);
    }

    public static String buildPath(Object[] parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        for (Object part : parts) {
            buf.append(part.toString());
            buf.append('>');
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 1); // remove trailing '>'
        }
        return buf.toString();
    }

    /**
     * Extract specified length of plain text from HTML.
     *
     * @param html the HTML to extract.
     * @param maxLen maximum length of extracted plain text.
     * @param keepCR whether insert CR between paragraphs, {@code true} to insert CR, {@code false} to insert blank space.
     * @return the extracted plain text
     */
    public static String getPlainText(String html, int maxLen, boolean keepCR) {
        final StringBuilder sb = new StringBuilder();
        HTMLEditorKit.ParserCallback parserCallback = new HTMLEditorKit.ParserCallback() {
            public boolean readyForNewline;

            @Override
            public void handleText(final char[] data, final int pos) {
                String s = new String(data);
                sb.append(s.trim());
                readyForNewline = true;
            }

            @Override
            public void handleStartTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
                if (readyForNewline && (t == HTML.Tag.DIV || t == HTML.Tag.BR || t == HTML.Tag.P)) {
                    sb.append(keepCR ? '\n' : ' ');
                    readyForNewline = false;
                }
            }

            @Override
            public void handleSimpleTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
                handleStartTag(t, a, pos);
            }
        };
        try {
            new ParserDelegator().parse(new StringReader(html), parserCallback, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sb.length() > maxLen) {
            char c = sb.charAt(maxLen);
            sb.setLength(maxLen);
            sb.append(c == ' ' ? " ..." : "...");
        }
        return escapeHtml(sb.toString());
    }
}
