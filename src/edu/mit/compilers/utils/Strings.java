package edu.mit.compilers.utils;

public class Strings {

    public static String escape(String input) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < input.length(); index++) {
            char c = input.charAt(index);
            switch (c) {
                case '\n': builder.append("\\n"); break;
                case '\\': builder.append("\\\\"); break;
                case '\'': builder.append("\\\'"); break;
                case '\"': builder.append("\\\""); break;
                default: builder.append(c);
            }
        }
        return builder.toString();
    }
}
