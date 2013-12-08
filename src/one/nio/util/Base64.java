package one.nio.util;

public final class Base64 {
    public static final byte[] DEFAULT_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public static final byte[] URL_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
    };

    private static final byte[] DECODE_TABLE = {
             0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 62,  0, 62,  0, 63,
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61,  0,  0,  0,  0,  0,  0,
             0,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,  0,  0,  0,  0, 63,
             0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    public static byte[] encode(byte[] s) {
        return encode(s, DEFAULT_TABLE);
    }

    public static byte[] encode(byte[] s, byte[] table) {
        final int len = s.length / 3 * 3;
        final byte[] result = new byte[(s.length + 2) / 3 << 2];

        int p = 0;
        for (int i = 0; i < len; i += 3, p += 4) {
            int v = (s[i] & 0xff) << 16 | (s[i + 1] & 0xff) << 8 | (s[i + 2] & 0xff);
            result[p]     = table[v >>> 18];
            result[p + 1] = table[(v >>> 12) & 0x3f];
            result[p + 2] = table[(v >>> 6) & 63];
            result[p + 3] = table[v & 0x3f];
        }

        switch (s.length - len) {
            case 1:
                result[p]     = table[(s[len] & 0xff) >> 2];
                result[p + 1] = table[(s[len] & 0x03) << 4];
                result[p + 2] = '=';
                result[p + 3] = '=';
                break;
            case 2:
                result[p]     = table[(s[len] & 0xff) >> 2];
                result[p + 1] = table[(s[len] & 0x03) << 4 | (s[len + 1] & 0xff) >> 4];
                result[p + 2] = table[(s[len + 1] & 0x0f) << 2];
                result[p + 3] = '=';
                break;
        }

        return result;
    }

    public static char[] encodeToChars(byte[] s) {
        return encodeToChars(s, DEFAULT_TABLE);
    }

    public static char[] encodeToChars(byte[] s, byte[] table) {
        final int len = s.length / 3 * 3;
        final char[] result = new char[(s.length + 2) / 3 << 2];

        int p = 0;
        for (int i = 0; i < len; i += 3, p += 4) {
            int v = (s[i] & 0xff) << 16 | (s[i + 1] & 0xff) << 8 | (s[i + 2] & 0xff);
            result[p]     = (char) table[v >>> 18];
            result[p + 1] = (char) table[(v >>> 12) & 0x3f];
            result[p + 2] = (char) table[(v >>> 6) & 63];
            result[p + 3] = (char) table[v & 0x3f];
        }

        switch (s.length - len) {
            case 1:
                result[p]     = (char) table[(s[len] & 0xff) >> 2];
                result[p + 1] = (char) table[(s[len] & 0x03) << 4];
                result[p + 2] = '=';
                result[p + 3] = '=';
                break;
            case 2:
                result[p]     = (char) table[(s[len] & 0xff) >> 2];
                result[p + 1] = (char) table[(s[len] & 0x03) << 4 | (s[len + 1] & 0xff) >> 4];
                result[p + 2] = (char) table[(s[len + 1] & 0x0f) << 2];
                result[p + 3] = '=';
                break;
        }

        return result;
    }

    public static byte[] decode(byte[] s) {
        int len = s.length;
        while (len > 0 && s[len - 1] == '=') {
            len--;
        }

        final int full = (len >> 2) * 3;
        final int pad = (len & 3) * 3 >> 2;
        final byte[] result = new byte[full + pad];
        final byte[] table = DECODE_TABLE;

        int i = 0;
        for (int p = 0; p < full; p += 3, i += 4) {
            int v = table[s[i]] << 18 | table[s[i + 1]] << 12 | table[s[i + 2]] << 6 | table[s[i + 3]];
            result[p]     = (byte) (v >>> 16);
            result[p + 1] = (byte) (v >>> 8);
            result[p + 2] = (byte) v;
        }

        switch (pad) {
            case 2:
                result[full + 1] = (byte) (table[s[i + 1]] << 4 | table[s[i + 2]] >> 2);
            case 1:
                result[full] = (byte) (table[s[i]] << 2 | table[s[i + 1]] >> 4);
        }

        return result;
    }

    public static byte[] decodeFromChars(char[] s) {
        int len = s.length;
        while (len > 0 && s[len - 1] == '=') {
            len--;
        }

        final int full = (len >> 2) * 3;
        final int pad = (len & 3) * 3 >> 2;
        final byte[] result = new byte[full + pad];
        final byte[] table = DECODE_TABLE;

        int i = 0;
        for (int p = 0; p < full; p += 3, i += 4) {
            int v = table[s[i]] << 18 | table[s[i + 1]] << 12 | table[s[i + 2]] << 6 | table[s[i + 3]];
            result[p]     = (byte) (v >>> 16);
            result[p + 1] = (byte) (v >>> 8);
            result[p + 2] = (byte) v;
        }

        switch (pad) {
            case 2:
                result[full + 1] = (byte) (table[s[i + 1]] << 4 | table[s[i + 2]] >> 2);
            case 1:
                result[full] = (byte) (table[s[i]] << 2 | table[s[i + 1]] >> 4);
        }

        return result;
    }
}
