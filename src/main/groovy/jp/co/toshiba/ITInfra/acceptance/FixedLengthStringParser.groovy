package jp.co.toshiba.ITInfra.acceptance

import groovy.util.logging.Slf4j

/**
 * 固定長文字列パーサ
 */
public class FixedLengthStringParser {

    /** 固定長文字列 */
    private final String source;
    /** 現在位置 */
    private int position = 0;

    /**
     * コンストラクタ
     * @param source 固定長文字列
     */
    public FixedLengthStringParser(String source) {
        this.source = source;
    }

    /**
     * 次の文字列を切り出す
     * @param length 長さ
     * @return 切り出した文字列
     */
    public String next(int length) {
        if (source == null) {
            return null;
        }
        if (length < 1) {
            return null;
        }
        if (position >= source.length()) {
            return null;
        }
        String ret = null;
        if (position + length > source.length()) {
            ret = source.substring(position);
            position += length;
            return ret;
        }
        ret = source.substring(position, position + length);
        position += length;
        return ret;
    }
}
