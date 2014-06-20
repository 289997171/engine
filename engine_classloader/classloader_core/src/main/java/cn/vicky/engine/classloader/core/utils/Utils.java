package cn.vicky.engine.classloader.core.utils;

/**
 * 工具类
 * 
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public class Utils {
    /**
     * Converts wildcard to regular expression
     * 
     * @param wildcard
     * @return regex
     */
    public static String wildcardToRegex(String wildcard) {
        StringBuilder s = new StringBuilder( wildcard.length() );
        s.append( '^' );
        for( int i = 0, is = wildcard.length(); i < is; i++ ) {
            char c = wildcard.charAt( i );
            switch (c) {
            case '*':
                s.append( ".*" );
                break;
            case '?':
                s.append( "." );
                break;
            case '(':
            case ')':
            case '[':
            case ']':
            case '$':
            case '^':
            case '.':
            case '{':
            case '}':
            case '|':
            case '\\':
                s.append( "\\" );
                s.append( c );
                break;
            default:
                s.append( c );
                break;
            }
        }
        s.append( '$' );
        return ( s.toString() );
    }
}
