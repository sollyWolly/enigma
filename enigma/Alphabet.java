package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Solomon Cheung
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _alpha = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Return _alpha. */
    String alpha() {
        return _alpha;
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _alpha.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _alpha.contains(Character.toString(ch));
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return (char) (_alpha.charAt(index));
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        return _alpha.indexOf(ch);
    }

    /** Variable. **/
    private String _alpha;

}
