package com.aircraftcarrier.framework.tookit;


/**
 * 目前最正确的emoji工具
 * 参考： {@link java.lang.Character#isEmoji}
 * maven: 15.0.1 刚起步还在完善中
 * coding: 15.0.2-SNAPSHOT
 *
 * @author lzp
 * @since 2023/07/25 10:08
 */
public class Emoji4jUtilTest {

    public static String getEmojiTestData() {
        // Others🥲😶‍🌫️😮‍💨😵‍💫🥸🥱❤️‍🔥❤️‍🩹🤎🤍👁️‍🗨️🤌🤏🦾🦿🦻🫀🫁🧔‍♂️🧔‍♀️🧑‍🦰🧑‍🦱🧑‍🦳🧑‍🦲👱‍♀️👱‍♂️🙍‍♂️🙍‍♀️🙎‍♂️🙎‍♀️🙅‍♂️🙅‍♀️🙆‍♂️🙆‍♀️💁‍♂️💁‍♀️🙋‍♂️🙋‍♀️🧏🧏‍♂️🧏‍♀️🤦‍♂️🧑‍⚕️🧑‍🎓🧑‍🏫🧑‍⚖️🧑‍🌾🧑‍🍳🧑‍🔧🧑‍🏭🧑‍💼🧑‍🔬🧑‍💻🧑‍🎤🧑‍🎨🧑‍✈️🧑‍🚀🧑‍🚒👮‍♂️👮‍♀️🕵️‍♂️🕵️‍♀️💂‍♂️💂‍♀️🥷👷‍♂️👷‍♀️👳‍♂️👳‍♀️🤵‍♂️🤵‍♀️👰‍♂️👰‍♀️👩‍🍼👨‍🍼🧑‍🍼🧑‍🎄💆‍♂️💆‍♀️💇‍♂️💇‍♀️🚶‍♂️🚶‍♀️🧍🧍‍♂️🧍‍♀️🧎🧎‍♂️🧎‍♀️🧑‍🦯👨‍🦯👩‍🦯🧑‍🦼👨‍🦼👩‍🦼🧑‍🦽👨‍🦽👩‍🦽🏃‍♂️🏃‍♀️👯‍♂️👯‍♀️🏄‍♂️🏄‍♀️🚣‍♂️🚣‍♀️🏊‍♂️🏊‍♀️⛹️‍♂️⛹️‍♀️🏋️‍♂️🏋️‍♀️🚴‍♂️🚴‍♀️🚵‍♂️🚵‍♀️🧑‍🤝‍🧑👩‍❤️‍💋‍👨👩‍❤️‍👨👨‍👦👨‍👦‍👦👨‍👧👨‍👧‍👦👨‍👧‍👧👩‍👦👩‍👦‍👦👩‍👧👩‍👧‍👦👩‍👧‍👧🫂🦰🦱🦳🦲🦧🦮🐕‍🦺🐈‍⬛🦬🦣🦫🐻‍❄️🦥🦦🦨🦤🪶🦩🦭🪲🪳🪰🪱🪴🫐🫒🫑🧄🧅🫓🧇🫔🧆🫕🧈🦪🫖🧋🧃🧉🧊🪨🪵🛖🛕🛻🦽🦼🛺🛼🪂🪐🤿🪀🪁🪄🪅🪆♟🪡🪢🦺🥻🩱🩲🩳🩴🩰🪖🪗🪕🪘🪔🪙🪓🪃🪚🪛🦯🪝🪜🩸🩹🩺🛗🪞🪟🪑🪠🪤🪒🪣🪥🪦🪧⏏♀♂⚧♾#️⃣*️⃣0️⃣1️⃣2️⃣3️⃣4️⃣5️⃣6️⃣7️⃣8️⃣9️⃣🟠🟡🟢🟣🟤🟥🟧🟨🟩🟦🟪🟫🏳️‍⚧️🏴‍☠️
        String s = "Others🥲😶‍🌫️😮‍💨😵‍💫🥸🥱##**❤️‍🔥❤️‍🩹🤎🤍👁️‍🗨️🤌🤏🦾🦿🦻🫀🫁🧔‍♂️🧔‍♀️🧑‍🦰🧑‍🦱🧑‍🦳🧑‍🦲👱‍♀️👱‍♂️🙍‍♂️🙍‍♀️🙎‍♂️🙎‍♀️🙅‍♂️🙅‍♀️🙆‍♂️🙆‍♀️💁‍♂️💁‍♀️🙋‍♂️🙋‍♀️🧏🧏‍♂️🧏‍♀️🤦‍♂️🧑‍⚕️🧑‍🎓🧑‍🏫🧑‍⚖️🧑‍🌾🧑‍🍳🧑‍🔧🧑‍🏭🧑‍💼🧑‍🔬🧑‍💻🧑‍🎤🧑‍🎨🧑‍✈️🧑‍🚀🧑‍🚒👮‍♂️👮‍♀️🕵️‍♂️🕵️‍♀️💂‍♂️💂‍♀️🥷👷‍♂️👷‍♀️👳‍♂️👳‍♀️🤵‍♂️🤵‍♀️👰‍♂️👰‍♀️👩‍🍼👨‍🍼🧑‍🍼🧑‍🎄💆‍♂️💆‍♀️💇‍♂️💇‍♀️🚶‍♂️🚶‍♀️🧍🧍‍♂️🧍‍♀️🧎🧎‍♂️🧎‍♀️🧑‍🦯👨‍🦯👩‍🦯🧑‍🦼👨‍🦼👩‍🦼🧑‍🦽👨‍🦽👩‍🦽🏃‍♂️🏃‍♀️👯‍♂️👯‍♀️🏄‍♂️🏄‍♀️🚣‍♂️🚣‍♀️🏊‍♂️🏊‍♀️⛹️‍♂️⛹️‍♀️🏋️‍♂️🏋️‍♀️🚴‍♂️🚴‍♀️🚵‍♂️🚵‍♀️🧑‍🤝‍🧑👩‍❤️‍💋‍👨👩‍❤️‍👨👨‍👦👨‍👦‍👦👨‍👧👨‍👧‍👦👨‍👧‍👧👩‍👦👩‍👦‍👦👩‍👧👩‍👧‍👦👩‍👧‍👧🫂🦰🦱🦳🦲🦧🦮🐕‍🦺🐈‍⬛🦬🦣🦫🐻‍❄️🦥🦦🦨🦤🪶🦩🦭🪲🪳🪰🪱🪴🫐🫒🫑🧄🧅🫓🧇🫔🧆🫕🧈🦪🫖🧋🧃🧉🧊🪨🪵🛖🛕🛻🦽🦼🛺🛼🪂🪐🤿🪀🪁🪄🪅🪆♟🪡🪢🦺🥻🩱🩲🩳🩴🩰🪖🪗🪕🪘🪔🪙🪓🪃🪚🪛🦯🪝🪜🩸🩹🩺🛗🪞🪟🪑🪠🪤🪒🪣🪥🪦🪧⏏♀♂⚧♾#️⃣*️⃣0️⃣1️⃣2️⃣3️⃣4️⃣5️⃣6️⃣7️⃣8️⃣9️⃣🟠🟡🟢🟣🟤🟥🟧🟨🟩🟦🟪🟫🏳️‍⚧️🏴‍☠️";
        String ss = "Others\uD83E\uDD72\uD83D\uDE36\u200D\uD83C\uDF2B️\uD83D\uDE2E\u200D\uD83D\uDCA8\uD83D\uDE35\u200D\uD83D\uDCAB\uD83E\uDD78\uD83E\uDD71❤️\u200D\uD83D\uDD25❤️\u200D\uD83E\uDE79\uD83E\uDD0E\uD83E\uDD0D\uD83D\uDC41️\u200D\uD83D\uDDE8️\uD83E\uDD0C\uD83E\uDD0F\uD83E\uDDBE\uD83E\uDDBF\uD83E\uDDBB\uD83E\uDEC0\uD83E\uDEC1\uD83E\uDDD4\u200D♂️\uD83E\uDDD4\u200D♀️\uD83E\uDDD1\u200D\uD83E\uDDB0\uD83E\uDDD1\u200D\uD83E\uDDB1\uD83E\uDDD1\u200D\uD83E\uDDB3\uD83E\uDDD1\u200D\uD83E\uDDB2\uD83D\uDC71\u200D♀️\uD83D\uDC71\u200D♂️\uD83D\uDE4D\u200D♂️\uD83D\uDE4D\u200D♀️\uD83D\uDE4E\u200D♂️\uD83D\uDE4E\u200D♀️\uD83D\uDE45\u200D♂️\uD83D\uDE45\u200D♀️\uD83D\uDE46\u200D♂️\uD83D\uDE46\u200D♀️\uD83D\uDC81\u200D♂️\uD83D\uDC81\u200D♀️\uD83D\uDE4B\u200D♂️\uD83D\uDE4B\u200D♀️\uD83E\uDDCF\uD83E\uDDCF\u200D♂️\uD83E\uDDCF\u200D♀️\uD83E\uDD26\u200D♂️\uD83E\uDDD1\u200D⚕️\uD83E\uDDD1\u200D\uD83C\uDF93\uD83E\uDDD1\u200D\uD83C\uDFEB\uD83E\uDDD1\u200D⚖️\uD83E\uDDD1\u200D\uD83C\uDF3E\uD83E\uDDD1\u200D\uD83C\uDF73\uD83E\uDDD1\u200D\uD83D\uDD27\uD83E\uDDD1\u200D\uD83C\uDFED\uD83E\uDDD1\u200D\uD83D\uDCBC\uD83E\uDDD1\u200D\uD83D\uDD2C\uD83E\uDDD1\u200D\uD83D\uDCBB\uD83E\uDDD1\u200D\uD83C\uDFA4\uD83E\uDDD1\u200D\uD83C\uDFA8\uD83E\uDDD1\u200D✈️\uD83E\uDDD1\u200D\uD83D\uDE80\uD83E\uDDD1\u200D\uD83D\uDE92\uD83D\uDC6E\u200D♂️\uD83D\uDC6E\u200D♀️\uD83D\uDD75️\u200D♂️\uD83D\uDD75️\u200D♀️\uD83D\uDC82\u200D♂️\uD83D\uDC82\u200D♀️\uD83E\uDD77\uD83D\uDC77\u200D♂️\uD83D\uDC77\u200D♀️\uD83D\uDC73\u200D♂️\uD83D\uDC73\u200D♀️\uD83E\uDD35\u200D♂️\uD83E\uDD35\u200D♀️\uD83D\uDC70\u200D♂️\uD83D\uDC70\u200D♀️\uD83D\uDC69\u200D\uD83C\uDF7C\uD83D\uDC68\u200D\uD83C\uDF7C\uD83E\uDDD1\u200D\uD83C\uDF7C\uD83E\uDDD1\u200D\uD83C\uDF84\uD83D\uDC86\u200D♂️\uD83D\uDC86\u200D♀️\uD83D\uDC87\u200D♂️\uD83D\uDC87\u200D♀️\uD83D\uDEB6\u200D♂️\uD83D\uDEB6\u200D♀️\uD83E\uDDCD\uD83E\uDDCD\u200D♂️\uD83E\uDDCD\u200D♀️\uD83E\uDDCE\uD83E\uDDCE\u200D♂️\uD83E\uDDCE\u200D♀️\uD83E\uDDD1\u200D\uD83E\uDDAF\uD83D\uDC68\u200D\uD83E\uDDAF\uD83D\uDC69\u200D\uD83E\uDDAF\uD83E\uDDD1\u200D\uD83E\uDDBC\uD83D\uDC68\u200D\uD83E\uDDBC\uD83D\uDC69\u200D\uD83E\uDDBC\uD83E\uDDD1\u200D\uD83E\uDDBD\uD83D\uDC68\u200D\uD83E\uDDBD\uD83D\uDC69\u200D\uD83E\uDDBD\uD83C\uDFC3\u200D♂️\uD83C\uDFC3\u200D♀️\uD83D\uDC6F\u200D♂️\uD83D\uDC6F\u200D♀️\uD83C\uDFC4\u200D♂️\uD83C\uDFC4\u200D♀️\uD83D\uDEA3\u200D♂️\uD83D\uDEA3\u200D♀️\uD83C\uDFCA\u200D♂️\uD83C\uDFCA\u200D♀️⛹️\u200D♂️⛹️\u200D♀️\uD83C\uDFCB️\u200D♂️\uD83C\uDFCB️\u200D♀️\uD83D\uDEB4\u200D♂️\uD83D\uDEB4\u200D♀️\uD83D\uDEB5\u200D♂️\uD83D\uDEB5\u200D♀️\uD83E\uDDD1\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC8B\u200D\uD83D\uDC68\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC68\uD83D\uDC68\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC67\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC69\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC66\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC67\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83E\uDEC2\uD83E\uDDB0\uD83E\uDDB1\uD83E\uDDB3\uD83E\uDDB2\uD83E\uDDA7\uD83E\uDDAE\uD83D\uDC15\u200D\uD83E\uDDBA\uD83D\uDC08\u200D⬛\uD83E\uDDAC\uD83E\uDDA3\uD83E\uDDAB\uD83D\uDC3B\u200D❄️\uD83E\uDDA5\uD83E\uDDA6\uD83E\uDDA8\uD83E\uDDA4\uD83E\uDEB6\uD83E\uDDA9\uD83E\uDDAD\uD83E\uDEB2\uD83E\uDEB3\uD83E\uDEB0\uD83E\uDEB1\uD83E\uDEB4\uD83E\uDED0\uD83E\uDED2\uD83E\uDED1\uD83E\uDDC4\uD83E\uDDC5\uD83E\uDED3\uD83E\uDDC7\uD83E\uDED4\uD83E\uDDC6\uD83E\uDED5\uD83E\uDDC8\uD83E\uDDAA\uD83E\uDED6\uD83E\uDDCB\uD83E\uDDC3\uD83E\uDDC9\uD83E\uDDCA\uD83E\uDEA8\uD83E\uDEB5\uD83D\uDED6\uD83D\uDED5\uD83D\uDEFB\uD83E\uDDBD\uD83E\uDDBC\uD83D\uDEFA\uD83D\uDEFC\uD83E\uDE82\uD83E\uDE90\uD83E\uDD3F\uD83E\uDE80\uD83E\uDE81\uD83E\uDE84\uD83E\uDE85\uD83E\uDE86♟\uD83E\uDEA1\uD83E\uDEA2\uD83E\uDDBA\uD83E\uDD7B\uD83E\uDE71\uD83E\uDE72\uD83E\uDE73\uD83E\uDE74\uD83E\uDE70\uD83E\uDE96\uD83E\uDE97\uD83E\uDE95\uD83E\uDE98\uD83E\uDE94\uD83E\uDE99\uD83E\uDE93\uD83E\uDE83\uD83E\uDE9A\uD83E\uDE9B\uD83E\uDDAF\uD83E\uDE9D\uD83E\uDE9C\uD83E\uDE78\uD83E\uDE79\uD83E\uDE7A\uD83D\uDED7\uD83E\uDE9E\uD83E\uDE9F\uD83E\uDE91\uD83E\uDEA0\uD83E\uDEA4\uD83E\uDE92\uD83E\uDEA3\uD83E\uDEA5\uD83E\uDEA6\uD83E\uDEA7⏏♀♂⚧♾#️⃣*️⃣0️⃣1️⃣2️⃣3️⃣4️⃣5️⃣6️⃣7️⃣8️⃣9️⃣\uD83D\uDFE0\uD83D\uDFE1\uD83D\uDFE2\uD83D\uDFE3\uD83D\uDFE4\uD83D\uDFE5\uD83D\uDFE7\uD83D\uDFE8\uD83D\uDFE9\uD83D\uDFE6\uD83D\uDFEA\uD83D\uDFEB\uD83C\uDFF3️\u200D⚧️\uD83C\uDFF4\u200D☠️";
        return s;
    }

    public static void main(String[] args) {
        System.out.println("Emoji4jUtil");
        String emojiTestData = getEmojiTestData();

        System.out.println(Emoji4jUtil.containsEmoji(emojiTestData));
        System.out.println(Emoji4jUtil.removeEmoji(emojiTestData));
        System.out.println(Emoji4jUtil.replaceAllEmoji(emojiTestData, "O"));

        System.out.println(Emoji4jUtil.removeEmoji("##**"));

        System.out.println("===========================");
        String s = "🤏";
        String ss = "🤏🦻";
        System.out.println(Emoji4jUtil.isEmojiChar(s));
        System.out.println(Emoji4jUtil.isEmojiChar(ss));
    }
}
