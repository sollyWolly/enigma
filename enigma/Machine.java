package enigma;

import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Solomon Cheung
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _rotors = new Rotor[numRotors];
        _pawls = pawls;
        for (Rotor r : allRotors) {
            _rotorMap.put(r.name(), r);
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _rotors.length;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i += 1) {
            _rotors[i] = _rotorMap.get(rotors[i]);
            _rotors[i].set(0);
            if (!_rotorMap.containsValue(_rotors[i])) {
                throw error("invalid rotor name");
            }
        }
        int n = 0;
        for (Rotor r: _rotors) {
            if (r instanceof MovingRotor) {
                n += 1;
            }
        }
        if (numPawls() != n) {
            throw error("bruh momento");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 0; i < setting.length(); i += 1) {
            _rotors[i + 1].set(setting.charAt(i));
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plug;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plug = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        c = _plug.wrap(c);
        c = plugboard().permute(c);

        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        boolean[] aMovedRotors = new boolean[_rotors.length];
        for (int i = 0; i < _rotors.length - 1; i += 1) {
            if (!_rotors[i + 1].atNotch()) {
                continue;
            }
            if (_rotors[i].rotates()) {
                aMovedRotors[i + 1] = true;
                _rotors[i + 1].advance();
            }
            if (!aMovedRotors[i]) {
                aMovedRotors[i] = true;
                _rotors[i].advance();
            }
        }
        if (!aMovedRotors[_rotors.length - 1]) {
            _rotors[_rotors.length - 1].advance();
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = numRotors() - 1; i >= 0; i -= 1) {
            c = _rotors[i].convertForward(c);
        }
        for (int i = 1; i < numRotors(); i += 1) {
            c = _rotors[i].convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String enc = "";
        for (int i = 0; i < msg.length(); i += 1) {
            enc += alphabet().toChar(convert(alphabet().toInt(msg.charAt(i))));
        }
        return enc;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    /** Variable. **/
    private Rotor[] _rotors;
    /** Variable. **/
    private int _pawls;
    /** Variable. **/
    private HashMap<String, Rotor> _rotorMap = new HashMap<String, Rotor>();
    /** Variable. **/
    private Permutation _plug;
}
