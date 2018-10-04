package com.serphacker.webaxe.requests;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class CharsetFixer {

    public static boolean canEncode(String charset, String text) {
        try {
            return canEncode(Charset.forName(charset), text);
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean canEncode(Charset charset, String text) {
        try {
            if (charset.canEncode()) {
                CharsetEncoder cse = charset.newEncoder();
                return cse.encode(CharBuffer.wrap(text)) != null;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String forceASCII(String sz) {
        if (sz == null || sz.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder(sz.length());

        for (int i = 0; i < sz.length(); i++) {
            switch (sz.charAt(i)) {
                case 'À':
                case 'Á':
                case 'Â':
                case 'Ã':
                case 'Ä':
                case 'Å':
                    builder.append('A');

                    break;
                case 'Ç':
                    builder.append('C');
                    break;

                case 'È':
                case 'É':
                case 'Ê':
                case 'Ë':
                    builder.append('E');
                    break;

                case 'Ì':
                case 'Í':
                case 'Î':
                case 'Ï':
                    builder.append('I');
                    break;

                case 'Ò':
                case 'Ó':
                case 'Ô':
                case 'Õ':
                case 'Ö':
                    builder.append('O');
                    break;

                case 'Ù':
                case 'Ú':
                case 'Û':
                case 'Ü':
                    builder.append('U');
                    break;

                case 'Ý':
                case 'Ÿ':
                    builder.append('Y');
                    break;

                case 'à':
                case 'á':
                case 'â':
                case 'ã':
                case 'ä':
                case 'å':
                    builder.append('a');
                    break;

                case 'ç':
                    builder.append('c');
                    break;

                case 'è':
                case 'é':
                case 'ê':
                case 'ë':
                    builder.append('e');
                    break;

                case 'ì':
                case 'í':
                case 'î':
                case 'ï':
                    builder.append('i');
                    break;

                case 'ð':
                case 'ò':
                case 'ó':
                case 'ô':
                case 'õ':
                case 'ö':
                    builder.append('o');
                    break;

                case 'ù':
                case 'ú':
                case 'û':
                case 'ü':
                    builder.append('u');
                    break;

                case 'ý':
                case 'ÿ':
                    builder.append('y');
                    break;

                default:
                    if (sz.charAt(i) < 128) {
                        builder.append(sz.charAt(i));
                    }
            }
        }
        return builder.toString();
    }

}