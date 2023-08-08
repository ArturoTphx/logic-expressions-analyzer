
import model.Analyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnalyzerTest {

    @Test
    @DisplayName("When the user insert an empty expression")
    public void empty_expression() {
        Analyzer analyzer = new Analyzer("");
        try {
            analyzer.reader();
        } catch (Exception e) {
            assert(true);
        }
    }

    @Test
    @DisplayName("When the user insert an expression with uppercase letters")
    public void uppercase_letters_expression() {
        Analyzer analyzer = new Analyzer("p*(p+q)<=>Q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) {
            assert(true);
        }
        analyzer = new Analyzer("P   + Q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) {
            assert(true);
        }
        analyzer = new Analyzer("R*P(r   + Q)");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) {
            assert(true);
        }
    }

    @Test
    @DisplayName("When the user insert an expression without balanced parentheses")
    public void bad_parentheses_expression() {
        Analyzer analyzer = new Analyzer("((x*y)=>r");
        try {
            analyzer.reader();
            assert(false);
        } catch (Exception e) { assert(true); }
        analyzer = new Analyzer("x*y)=>r");
        try {
            analyzer.reader();
            assert(false);
        } catch (Exception e) { assert(true); }
        analyzer = new Analyzer("x*y))=>r");
        try {
            analyzer.reader();
            assert(false);
        } catch (Exception e) { assert(true); }
        analyzer = new Analyzer("((x*y)+(q)=>r");
        try {
            analyzer.reader();
            assert(false);
        } catch (Exception e) { assert(true); }
    }

    // First case type of syntax error:
    // + p + q or p + q +
    // p + q -
    // -> p + q or p + q ->
    // => p + q or p + q =>
    // <- p + q or p + q <-
    // <= p + q or p + q <=
    // <-> p + q or p + q <->
    // <=> p + q or p + q <=>
    @Test
    @DisplayName("When the user insert a first case expression")
    public void first_case_expression() {
        Analyzer analyzer = new Analyzer("+ p + q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true);}
        analyzer = new Analyzer("p + q +");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true);}
        analyzer = new Analyzer("p + q -");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true);}
        analyzer = new Analyzer("-> p + q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true);}
        analyzer = new Analyzer("p + q ->");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) {assert (true);}
        analyzer = new Analyzer("=> p + q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) {assert (true); }
        analyzer = new Analyzer("p + q =>");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true);}
        analyzer = new Analyzer("<- p + q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) {assert (true);}
        analyzer = new Analyzer("p + q <-");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
        analyzer = new Analyzer("<= p + q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
        analyzer = new Analyzer("p + q <=");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
        analyzer = new Analyzer("<-> p + q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
        analyzer = new Analyzer("p + q <->");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
        analyzer = new Analyzer("<=> p + q");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
        analyzer = new Analyzer("p + q <=>");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
    }

    // Pattern that verifies if the given expression have someone of the following symbols:
    //  =< or >= or >-< or <-< or >-> or >=< or <=< or >=>
    // Examples:
    // p + q >=< r or p + q >-< or ...
    @Test
    @DisplayName("When the user insert a second case expression")
    public void second_case_expression() {
        Analyzer analyzer = new Analyzer("p + q>=     < r");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
        analyzer = new Analyzer("p + r > -    < v");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
        analyzer = new Analyzer("r > - <");
        try {
            analyzer.reader();
            assert (false);
        } catch (Exception e) { assert (true); }
    }

}