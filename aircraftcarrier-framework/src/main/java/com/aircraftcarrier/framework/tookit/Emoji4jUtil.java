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
    private GraphemeMatcher graphemeMatcher;


    private Emoji4jUtil(String source) {
        this.graphemeMatcher = newGraphemeMatcher(source);
    }

    public static Emoji4jUtil newInstance(String input) {
        return new Emoji4jUtil(input);
    }

    public GraphemeMatcher newGraphemeMatcher(String input) {
        DefaultGraphemeTrie trie = Graphemes.getDefaultTrie();
        return new GraphemeMatcher(trie, input);
    }

    /**
     * isEmojiChar
     */
    public boolean isEmojiChar() {
        return graphemeMatcher.matches();
    }

    /**
     * containsEmoji
     */
    public boolean containsEmoji() {
        return graphemeMatcher.find();
    }


    /**
     * removeEmoji
     */
    public String removeEmoji() {
        return graphemeMatcher.replaceAll("");
    }

    /**
     * replaceAllEmoji
     */
    public String replaceAllEmoji(String replacer) {
        return graphemeMatcher.replaceAll(replacer);
    }


}
