package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Solomon Cheung
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine mach1 = readConfig();
        if (_input.hasNext("\\*")) {
            while (_input.hasNextLine()) {
                String name = _input.nextLine();
                if (name.isEmpty()) {
                    printMessageLine("");
                } else if (name.charAt(0) == '*') {
                    setUp(mach1, name);
                } else {
                    Scanner space = new Scanner(name);
                    String solstring = "";
                    while (space.hasNext()) {
                        solstring += space.next();
                    }
                    solstring = mach1.convert(solstring);
                    printMessageLine(solstring);
                }
            }
        } else {
            throw error("missing settings");
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alpha = _config.nextLine().trim();
            _alphabet = new Alphabet(alpha);
            if (!_config.hasNextInt()) {
                throw error("invalid rotor value");
            }
            int rotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw error("invalid pawl value");
            }
            int pawls = _config.nextInt();
            ArrayList<Rotor> list = new ArrayList<>();
            while (_config.hasNext()) {
                list.add(readRotor());
            }
            return new Machine(_alphabet, rotors, pawls, list);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String setting = _config.next();
            ArrayList<String> perm = new ArrayList<String>();
            int open = 0;
            int close = 0;
            while (_config.hasNext("\\"
                    + "s*[(].*[)]?")) {
                String next = _config.next();
                if (next.charAt(0) == '(') {
                    open += 1;
                }
                if (next.charAt(next.length() - 1) == ')') {
                    close += 1;
                }
                perm.add(next);
            }
            if (open != close) {
                throw error("bang");
            }
            if (setting.charAt(0) == 'M') {
                return new MovingRotor(name,
                        new Permutation(perm.toString(), _alphabet),
                        setting.substring(1));
            } else if (setting.charAt(0) == 'N') {
                return new FixedRotor(name,
                        new Permutation(perm.toString(), _alphabet));
            } else if (setting.charAt(0) == 'R') {
                return new Reflector(name,
                        new Permutation(perm.toString(), _alphabet));
            } else {
                return null;
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner b = new Scanner(settings);
        b.next();
        String[] insert = new String[M.numRotors()];
        for (int i = 0; i < M.numRotors(); i += 1) {
            insert[i] = b.next();
        }
        for (int i = 0; i < insert.length - 1; i += 1) {
            for (int j = 0; j < insert.length - 1; j += 1) {
                if (i != j && insert[i].equals(insert[j])) {
                    throw error("duplicate rotor");
                }
            }
        }
        if (!b.hasNext()) {
            throw error("bad wheel settings");
        }
        String s1 = b.next();
        M.insertRotors(insert);
        M.setRotors(s1);
        String bang = "";
        while (b.hasNext("\\s*[(].*[)]")) {
            bang += b.next();
        }
        M.setPlugboard(new Permutation(bang, _alphabet));
        if (b.hasNext()) {

        }
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String prt = "";
        for (int i = 0; i < msg.length(); i += 1) {
            if (i != 0 && i % 5 == 0) {
                System.out.print(prt);
                prt = " " + msg.substring(i, i + 1);
            } else {
                prt += msg.substring(i, i + 1);
            }
        }
        System.out.println(prt);
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** True if --verbose specified. */
    private int num;
}
