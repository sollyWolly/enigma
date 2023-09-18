package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Solomon Cheung
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String [] arr = cycles.replace("(",
                " ").replace(")",
                " ").split("\\s+");
        for (String b: arr) {
            if (b.length() != 0) {
                _cycle.add(b);
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycle.add(cycle.replace("(",
                "").replace(")", ""));
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _alphabet.alpha().indexOf(
                permute(_alphabet.alpha().charAt(wrap(p))));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _alphabet.alpha().indexOf(
                invert(_alphabet.alpha().charAt(wrap(c))));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        for (String b: _cycle) {
            for (int i = 0; i < b.length(); i += 1) {
                if (b.charAt(i) == p) {
                    if (i == b.length() - 1) {
                        return b.charAt(0);
                    } else {
                        return b.charAt(i + 1);
                    }
                }
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        for (String b: _cycle) {
            for (int i = 0; i < b.length(); i += 1) {
                if (b.charAt(i) == c) {
                    if (i == 0) {
                        return b.charAt(b.length() - 1);
                    } else {
                        return b.charAt(i - 1);
                    }
                }
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        boolean flag = true;
        for (int i = 0; i < alphabet().size(); i += 1) {
            flag = true;
            for (String b : _cycle) {
                for (int c = 0; c < b.length(); c += 1) {
                    if (b.charAt(c) == alphabet().alpha().charAt(i)) {
                        flag = false;
                    }
                }
            }
            if (!flag) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Variable. **/
    private java.util.ArrayList<String> _cycle = new java.util.ArrayList<>();
}
