package com.aircraftcarrier.framework.tookit;

import com.sigpwned.emoji4j.core.GraphemeMatcher;
import com.sigpwned.emoji4j.core.trie.DefaultGraphemeTrie;
import com.sigpwned.emoji4j.core.util.Graphemes;

/**
 * emoji
 *
 * @author ext.liuzhipeng12
 * @since 2023/07/25 10:03
 */
public class Emoji4jUtil {
    private Emoji4jUtil() {

    }

    private static GraphemeMatcher newGraphemeMatcher(String input) {
        DefaultGraphemeTrie trie = Graphemes.getDefaultTrie();
        return new GraphemeMatcher(trie, input);
    }

    /**
     * isEmoji
     */
    public static boolean isEmojiChar(String str) {
        return newGraphemeMatcher(str).matches();
    }

    /**
     * containsEmoji
     */
    public static boolean containsEmoji(String str) {
        return newGraphemeMatcher(str).find();
    }


    /**
     * removeEmoji
     */
    public static String removeEmoji(String str) {
        return newGraphemeMatcher(str).replaceAll(r -> "");
    }
}
