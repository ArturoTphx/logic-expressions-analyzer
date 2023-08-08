package model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analyzer {

    public String clean_expression;
    public String expression;
    public String recover_expression;
    public String simplified_expression;
    public String disjunctive;
    public String conjunctive;
    public HashMap<Character, Character> symbols;
    public HashMap<String, String> truth_table;
    public HashMap<String, String> substitutions;
    public Integer substitution_id;
    public List<String> operators;

    public Analyzer(String expression) {
        this.expression = expression;
        this.symbols = new HashMap<>();
        this.truth_table = new HashMap<>();
        this.substitutions = new HashMap<>();
        this.substitution_id = 1;
        this.operators = new ArrayList<>();
        operators.add("\\*");
        operators.add("\\+");
        operators.add("->");
        operators.add("=>");
        operators.add("<->");
        operators.add("<=>");
    }

    // Method in charge of read and analyze the syntax of the expression and store the corresponding symbols
    public void reader() throws Exception {
        // First we remove all the spaces in the expression for better manipulation
        this.expression = this.expression.replaceAll("\\s+", "");

        // Second we verify if the given expression is blank or empty
        if (this.expression.isBlank() || this.expression.isEmpty()) {
            throw new Exception("The expression is empty!");
        }

        // Pattern that verifies if the given expression has uppercase symbols (except V and F)
        Pattern upper = Pattern.compile(".*(?![VF])[A-Z].*");
        Matcher u = upper.matcher(this.expression);
        if (u.find()) {
            throw new Exception("Uppercase symbols are not allowed!");
        }

        // Pattern that verifies if the given expression has numbers
        Pattern number = Pattern.compile(".*\\d.*");
        Matcher n = number.matcher(this.expression);
        if (n.find()) {
            throw new Exception("Numbers are not allowed!");
        }

        // Pattern that verifies if the given expression has someone of the following cases:
        // + p + q or p + q +
        // * p + q or p + q *
        // p + q -
        // -> p + q or p + q ->
        // => p + q or p + q =>
        // <- p + q or p + q <-
        // <= p + q or p + q <=
        // <-> p + q or p + q <->
        // <=> p + q or p + q <=>
        Pattern firstCase = Pattern.compile("(.*\\+)|(\\+.*)|(.*\\*)|(\\*.*)|(.*-)|(.*<-)|(<-.*)|(.*->)|(->.*)|(.*<=)|(<=.*)|(.*=>)|(=>.*)|(.*<->)|(<->.*)|(.*<=>)|(<=>.*)");
        Matcher fc = firstCase.matcher(this.expression);
        if (fc.matches()) {
            throw new Exception("The expression has bad syntax!");
        }

        // Pattern that verifies if the given expression has someone of the following symbols:
        //  =< or >= or >-< or <-< or >-> or >=< or <=< or >=>
        Pattern secondCase = Pattern.compile("(.*=<.*)|(.*>=.*)|(.*>-<.*)|(.*<-<.*)|(.*>->.*)|(.*>=<.*)|(.*<=<.*)|(.*>=>.*)");
        Matcher sc = secondCase.matcher(this.expression);
        if (sc.matches()) {
            throw new Exception("The expression has bad syntax!");
        }

        // Pattern that verifies if the given expression has someone of the following cases:
        // p + q -* r or p + q -+ r or p-q or () or (-)
        Pattern thirdCase = Pattern.compile(".*-(\\*|\\+|<-|->|<=|=>|<->|<=>).*|.*[a-z]-[a-z].*|.*\\(-?\\).*");
        Matcher tc = thirdCase.matcher(this.expression);
        if (tc.matches()) {
            throw new Exception("The expression has bad syntax!");
        }

        // Pattern that checks if the expression has a continuous duplicate of operators
        Pattern fourthCase = Pattern.compile(".*(\\*{2,}|\\+{2,}|(<-){2,}|(->){2,}|(<=){2,}|(=>){2,}|(<->){2,}|(<=>){2,}).*");
        Matcher foc = fourthCase.matcher(this.expression);
        if (foc.matches()) {
            throw new Exception("The expression has bad syntax!");
        }

        // Pattern that verifies if a given character does not match with some of the following symbols:
        // [ *, -, +, (, ), >, <, = ]
        Pattern inner = Pattern.compile("\\*|-|\\+|\\(|\\)|>|<|=");
        // Stack that verifies if the parentheses in the expression are balanced
        Stack<Character> parentheses = new Stack<>();
        // String Builder in charge to store the resulting expression
        // after the first read
        StringBuilder clean = new StringBuilder();
        char before = ' ', present = ' ';
        // Now we read character by character the expression
        for (Character e : this.expression.toCharArray()) {
            before = present;
            present = e;
            // If we read an opening parentheses, we push it in the stack
            if (e == '(') {
                parentheses.push(e);
            }
            // If we read a closing parentheses, we pop an element in the stack
            if (e == ')') {
                // If the stack is not empty we can go ahead and pop some element
                // In the other hand, if the stack is empty, we throw an exception
                // because of unbalanced parentheses
                if (!parentheses.isEmpty()) {
                    parentheses.pop();
                } else {
                    throw new Exception("The expression does not have the balanced parentheses!");
                }
            }
            // If a character does not match with an especial operator,
            // we proceed to put them in a symbol HashMap for future
            // processes
            Matcher matcher = inner.matcher(e.toString());
            if (!matcher.matches()) {
                symbols.put(e, e);
            }
            // If there are two minus consecutive operators, we proceed to
            // simplify that:
            // p*--q => p*q
            if (before == '-' && present == '-') {
                before = ' ';
                present = ' ';
                clean = new StringBuilder(clean.substring(0, clean.length() - 1));
            } else {
                clean.append(e);
            }
        }
        // We store the 'clean' string in the clean_expression attribute
        this.clean_expression = clean.toString();
        // We remove 'V' and 'F' of the symbol HashMap
        symbols.remove('V');
        symbols.remove('F');
        // If the symbols size is grater than 8, we throw an exception,
        // because we only allow 8 or fewer variables
        if (symbols.size() > 8) {
            throw new Exception("The expression has more than eight variables!");
        }
        // If the Stack parentheses is not empty, that means that the expression
        // have unbalanced parenthesis
        if (!parentheses.isEmpty()) {
            throw new Exception("The expression does not have the balanced parentheses!");
        }
    }

    // Method that creates the truth table of the expression
    public void build_truth_table() throws Exception {
        if (symbols.size() <= 8) {
            // If we define n as the number of symbols obtained,
            // we can have the numbers of rows of the truth table
            // resolving this formula: 2^n
            int rows = (int) Math.pow(2, symbols.size());
            // The first variable has a number of continuous true or false rows equivalent
            // to (2^n)/n = x
            // And the second has a number of continuous true or false rows equivalent
            // to x/2 = y
            // ...
            // Ej:
            // 2^n = 4 : n = 2
            // distribution = (2^n)/2 = (4/2) = 2 = x
            // 2 true rows and 2 false rows
            // p -> V V F F
            // distribution = x/2 = 2/2 = 1
            // 1 true rows and 1 false rows
            // q -> V F V F
            int distribution = rows / 2;
            for (Character s : symbols.values()) {
                // We proceed to insert into the truth table hash map the reading symbol in
                // its positive and negative form to further use
                this.truth_table.put(s.toString(), this.truth_table.getOrDefault(s.toString(), ""));
                this.truth_table.put("-" + s, this.truth_table.getOrDefault("-" + s, ""));
                boolean change = true;
                int total_sum = 0;
                int dist_sum = 0;
                // This loop fill the truth table with the rows of each variable according to the size
                // of rows previously obtained 2^n and the distribution
                while (total_sum != rows) {
                    if (dist_sum == distribution) {
                        dist_sum = 0;
                        change = !change;
                    }
                    if (change) {
                        this.truth_table.put(s.toString(), this.truth_table.get(s.toString()) + "V");
                        this.truth_table.put("-" + s, this.truth_table.get("-" + s) + "F");
                    } else {
                        this.truth_table.put(s.toString(), this.truth_table.get(s.toString()) + "F");
                        this.truth_table.put("-" + s, this.truth_table.get("-" + s) + "V");
                    }
                    dist_sum++;
                    total_sum++;
                }
                // We update the distribution of the rows for the next variable
                distribution = distribution / 2;
            }
            // If we only have one symbol and the clean expression attribute has a size of 1
            // we do not touch anymore the truth table
            // Ej:
            // clean_expression = "p"
            // truth_table = { "p" -> "VF", "-p" -> "FV" }
            if (symbols.size() == 1 && clean_expression.length() == 1) {
                return;
            }
            // First we call the process substitutions method in order to
            // resolve and substitute variables at the same time
            String expression_substitutions = process_substitutions(this.clean_expression);
            // Lastly we call the expression solver one last time, because the expression_substitutions variable
            // always have only one number at the final of the process
            // Ej:
            // p*q => 1
            // p*(p+q) => (p+q) => 1 => p*1 => 2
            expression_solver(expression_substitutions);
            // The following method only reconstructs the expressions based
            // on the substitutions that were made for solving
            // the original expression
            ArrayList<String> subs_keys = new ArrayList<>(this.substitutions.keySet());
            Collections.sort(subs_keys);
            for (String key : subs_keys) {
                String after_subs = this.substitutions.get(key);
                Pattern number = Pattern.compile("\\d+");
                Matcher matcher = number.matcher(after_subs);
                while (matcher.find()) {
                    String element = matcher.group(0);
                    after_subs = after_subs.replaceAll(element, "(" + this.substitutions.get(element) + ")");
                    matcher = number.matcher(after_subs);
                }
                this.substitutions.put(key, after_subs);
                String temp_positive_tt = this.truth_table.get(key);
                String check_for_negative = this.truth_table.get("-" + key);
                if (check_for_negative != null) {
                    this.truth_table.remove("-" + key);
                    this.truth_table.put("-(" + after_subs + ")", check_for_negative);
                }
                this.truth_table.remove(key);
                this.truth_table.put(after_subs, temp_positive_tt);
                this.recover_expression = after_subs;
            }
        } else {
            throw new Exception("Invalid number of variables!");
        }
    }

    // Process in charge of creating and resolving the substitutions
    // Ej:
    // Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V,}
    // p+(p*q)
    // Step 1: (p*q) = 1 | Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F F F }
    // Step 2: p+(p*q) = p+1 = 2 | Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F F F, 2 -> V V F F }
    // Other ej:
    // p*q+-(q*p)
    // Step 1: (q*p) = 1 | Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F F F }
    // Step 2: p*q+-1 | Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F F F, -1 -> F V V V }
    // Step 3: p*q+-1 = 2+-1 | Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F F F, -1 -> F V V V, 2 -> V F F F }
    // Step 4: 2+-1 = 3 | Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F F F, -1 -> F V V V, 2 -> V F F F, 3 -> V V V V }
    private String process_substitutions(String expression_part) {
        Pattern parentheses_check = Pattern.compile("\\((?:[^)(]+|\\((?:[^)(]+|\\([^)(]*\\))*\\))*\\)");
        Matcher parentheses_matcher = parentheses_check.matcher(expression_part);
        while (parentheses_matcher.find()) {
            String inner_group = parentheses_matcher.group(0);
            expression_part = expression_part.replace(inner_group, recursive_parenthesis(inner_group.substring(1, inner_group.length() - 1)));
            parentheses_matcher = parentheses_check.matcher(expression_part);
        }
        return expression_part;
    }

    // Analyze the parenthesis of an expression in recursion and iteration simultaneously
    // Ej:
    // recursive_parenthesis("p*(-p*(q*q))")
    // Step 1: -p*(q*q) found into parentheses, so now we call recursive_parenthesis("-p*(q*q)")
    // Step 2: q*q found into parentheses, so now we call recursive_parenthesis("q*q")
    // Step 3: q*q hast not parentheses, so now we resolve substitute q*q = 1 and resolve it
    //         | Truth Table = { p -> V V F F, q -> V F V F, 1 -> V F V F }
    // Step 4: return new value of q*q (1) to the other call and substitute:
    //          -p*(q*q) => -p*1 Now we can substitute and resolve:
    //          -p*1 => 2
    //         | Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F V F, 2 -> F F V F }
    // Step 5: Return 2 to the original call and substitute:
    // p*(-p*(q*q)) => p*2
    // Then substitute and resolve:
    // p*2 => 3
    // Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F V F, 2 -> F F V F, 3 -> F F F F}
    private String recursive_parenthesis(String expression_part) {
        Pattern parentheses_check = Pattern.compile("\\((?:[^)(]+|\\((?:[^)(]+|\\([^)(]*\\))*\\))*\\)");
        Matcher parentheses_matcher = parentheses_check.matcher(expression_part);
        if (parentheses_matcher.find()) {
            String inner_group = parentheses_matcher.group(0);
            String subs = expression_part.replace(inner_group, recursive_parenthesis(inner_group.substring(1, inner_group.length() - 1)));
            Matcher inner = parentheses_check.matcher(subs);
            if (inner.find()) {
                subs = process_substitutions(subs);
            }
            String expression = expression_solver(subs);
            this.substitutions.put(expression, subs);
            return expression;
        }
        String expression = expression_solver(expression_part);
        this.substitutions.put(expression, expression_part);
        return expression;
    }

    // Method in charge of solving the expressions
    // It always recognizes a single value or a pair of values
    private String expression_solver(String expression) {
        // If a single value is detected,
        // we substitute it with a number and
        // make a truth table for that substitution
        // Ej:
        // p, -p, q, -q, ...
        if (expression.matches("-?[a-z]")) {
            String id = substitution_id.toString();
            this.substitutions.put(id, expression);
            this.truth_table.put(id, this.truth_table.get(expression));
            expression = expression.replace(expression, id);
            substitution_id++;
            return expression;
        }
        // If true (V) of false (F) is detected,
        // we substitute it with a number and
        // make a truth table for that substitution
        // Ej:
        // V, -V, F, -F
        if (expression.matches("-?[V|F]")) {
            String id = substitution_id.toString();
            this.substitutions.put(id, expression);
            this.truth_table.put(id, expression.repeat((int) Math.pow(2, symbols.size())));
            expression = expression.replace(expression, id);
            substitution_id++;
            return expression;
        }
        // Regex that catches a symbol:
        // It can be a lowercase letter, V, F, or a number
        // It also detects a minus operator
        // With that we can have a group with the left
        // and right symbols
        String symbol_regex = "(-?[a-z|V|F]|-?\\d*)";
        // Iterates through the list of operators in order to resolve
        // the expression in the correct order
        int operator_index = 0;
        // The expression always is going to be converted into a number,
        // if so, we return it
        while (!expression.matches("\\d+")) {
            // This loop iterates through the operator list
            while (operator_index < this.operators.size()) {
                // Current operator selected in the list of operators
                String current_operator = this.operators.get(operator_index);
                // Pattern that recognizes three groups:
                // 1. left_symbol, 2. operator, 3. right_symbol
                Pattern operator_check = Pattern.compile(symbol_regex + current_operator + symbol_regex);
                Matcher operator_matcher = operator_check.matcher(expression);
                // If the regex matches the expression, we analyze that
                while (operator_matcher.find()) {
                    String left_symbol = operator_matcher.group(1);
                    String right_symbol = operator_matcher.group(2);
                    // If symbols are empty, break the cycle to change operator
                    if ((left_symbol.isBlank() | left_symbol.isEmpty()) || (right_symbol.isBlank() | right_symbol.isEmpty())) {
                        break;
                    }
                    String id;
                    // These conditions store the substitution with its id an original form in a Hash Map
                    // and replace the expression
                    // Ej:
                    // p*p+q
                    // Step 1: detects * operator and groups: left_symbol: P: right_symbol: p
                    // Step 2: substitute p*p = 1
                    // Step 3: substitute in expression: 1+q
                    // Step 4: resolve 1
                    if (Objects.equals(current_operator, "\\*")) {
                        id = substitution_id.toString();
                        this.substitutions.put(id, left_symbol + "*" + right_symbol);
                        expression = expression.replace(left_symbol + "*" + right_symbol, id);
                        substitution_id++;
                    } else if (Objects.equals(current_operator, "\\+")) {
                        id = substitution_id.toString();
                        this.substitutions.put(id, left_symbol + "+" + right_symbol);
                        expression = expression.replace(left_symbol + "+" + right_symbol, id);
                        substitution_id++;
                    } else {
                        id = substitution_id.toString();
                        this.substitutions.put(id, left_symbol + current_operator + right_symbol);
                        expression = expression.replace(left_symbol + current_operator + right_symbol, id);
                        substitution_id++;
                    }
                    String left_symbol_tt;
                    String right_symbol_tt;
                    boolean left_especial = left_symbol.equals("V") | left_symbol.equals("F") | left_symbol.equals("-V") | left_symbol.equals("-F");
                    boolean right_especial = right_symbol.equals("V") | right_symbol.equals("F") | right_symbol.equals("-V") | right_symbol.equals("-F");
                    // Now we verify if the left or right symbol is an especial true or false symbol, if so, we build a temporary
                    // truth table with its values. In the other case, we only get the truth table with the symbol key
                    if (left_especial) {
                        if (left_symbol.equals("-V")) {
                            left_symbol = "F";
                        } else if (left_symbol.equals("-F")) {
                            left_symbol = "V";
                        }
                        left_symbol_tt = left_symbol.repeat((int) Math.pow(2, symbols.size()));
                    } else {
                        if (left_symbol.charAt(0) == '-') {
                            String positive_left_symbol_tt = this.truth_table.get(left_symbol.substring(1));
                            StringBuilder negative_left_symbol_tt = new StringBuilder();
                            for (Character s : positive_left_symbol_tt.toCharArray()) {
                                if (s == 'V') {
                                    negative_left_symbol_tt.append("F");
                                }
                                if (s == 'F') {
                                    negative_left_symbol_tt.append("V");
                                }
                            }
                            this.truth_table.put(left_symbol, negative_left_symbol_tt.toString());
                        }
                        left_symbol_tt = this.truth_table.get(left_symbol);
                    }
                    if (right_especial) {
                        if (right_symbol.equals("-V")) {
                            right_symbol = "F";
                        } else if (right_symbol.equals("-F")) {
                            right_symbol = "V";
                        }
                        right_symbol_tt = right_symbol.repeat((int) Math.pow(2, symbols.size()));
                    } else {
                        if (right_symbol.charAt(0) == '-') {
                            String positive_right_symbol_tt = this.truth_table.get(right_symbol.substring(1));
                            StringBuilder negative_right_symbol_tt = new StringBuilder();
                            for (Character s : positive_right_symbol_tt.toCharArray()) {
                                if (s == 'V') {
                                    negative_right_symbol_tt.append("F");
                                }
                                if (s == 'F') {
                                    negative_right_symbol_tt.append("V");
                                }
                            }
                            this.truth_table.put(right_symbol, negative_right_symbol_tt.toString());
                        }
                        right_symbol_tt = this.truth_table.get(right_symbol);
                    }
                    // Depending on the operator we choose the according rules to evaluate
                    // Each method returns a string with the values of the truth table,
                    // so we insert that into the Truth Table with the previous
                    // substitution id}
                    // Ej:
                    // p*q
                    // p*q => 1
                    // and_rules("p*q") = "VFFF"
                    // Truth Table = { p -> V V F F, -p -> F F V V, q -> V F V F, -q -> F V F V, 1 -> V F F F }
                    switch (current_operator) {
                        case "\\*": {
                            this.truth_table.put(id, and_rules(left_symbol_tt, right_symbol_tt));
                            break;
                        }
                        case "\\+": {
                            this.truth_table.put(id, or_rules(left_symbol_tt, right_symbol_tt));
                            break;
                        }
                        case "->":
                        case "=>": {
                            truth_table.put(id, implies_rules(left_symbol_tt, right_symbol_tt));
                            break;
                        }
                        case "<->":
                        case "<=>": {
                            this.truth_table.put(id, ioi_rules(left_symbol_tt, right_symbol_tt));
                            break;
                        }
                    }
                    // We overwrite the operator_matcher with the substitutions
                    // for checking other same operators
                    operator_matcher = operator_check.matcher(expression);
                }
                // Iterate through the list of operators in if there are no more matches
                operator_index++;
            }
        }
        return expression;
    }

    // Rules of and operator: *
    private String and_rules(String left_symbol_tt, String right_symbol_tt) {
        StringBuilder result_truth_table = new StringBuilder();
        int index_of_truth_table = 0;
        int max_index = (int) Math.pow(2, symbols.size()) - 1;
        while (index_of_truth_table <= max_index) {
            if (left_symbol_tt.charAt(index_of_truth_table) == 'V' && right_symbol_tt.charAt(index_of_truth_table) == 'V') {
                result_truth_table.append("V");
            } else {
                result_truth_table.append("F");
            }
            index_of_truth_table++;
        }
        return result_truth_table.toString();
    }

    // Rules of or operator: +
    private String or_rules(String left_symbol_tt, String right_symbol_tt) {
        StringBuilder result_truth_table = new StringBuilder();
        int index_of_truth_table = 0;
        int max_index = (int) Math.pow(2, symbols.size()) - 1;
        while (index_of_truth_table <= max_index) {
            if (left_symbol_tt.charAt(index_of_truth_table) == 'F' && right_symbol_tt.charAt(index_of_truth_table) == 'F') {
                result_truth_table.append("F");
            } else {
                result_truth_table.append("V");
            }
            index_of_truth_table++;
        }
        return result_truth_table.toString();
    }

    // Rules implies and operator: ->, =>
    private String implies_rules(String left_symbol_tt, String right_symbol_tt) {
        StringBuilder result_truth_table = new StringBuilder();
        int index_of_truth_table = 0;
        int max_index = (int) Math.pow(2, symbols.size()) - 1;
        while (index_of_truth_table <= max_index) {
            if (left_symbol_tt.charAt(index_of_truth_table) == 'V' && right_symbol_tt.charAt(index_of_truth_table) == 'F') {
                result_truth_table.append("F");
            } else {
                result_truth_table.append("V");
            }
            index_of_truth_table++;
        }
        return result_truth_table.toString();
    }

    // Rules if only if and operator: <->, <=>
    private String ioi_rules(String left_symbol_tt, String right_symbol_tt) {
        StringBuilder result_truth_table = new StringBuilder();
        int index_of_truth_table = 0;
        int max_index = (int) Math.pow(2, symbols.size()) - 1;
        while (index_of_truth_table <= max_index) {
            if (left_symbol_tt.charAt(index_of_truth_table) == right_symbol_tt.charAt(index_of_truth_table)) {
                result_truth_table.append("V");
            } else {
                result_truth_table.append("F");
            }
            index_of_truth_table++;
        }
        return result_truth_table.toString();
    }

    // This method is in charge of simplify the given expression
    public void simplify() {
        // We have to use the reconstructed expression because we are going
        // to change the equivalent truth table symbols based on the recover
        // substitutions
        this.simplified_expression = this.recover_expression;
        ArrayList<String> truth_keys = new ArrayList<>(this.truth_table.keySet());
        // We have to sort the list in ascending order, because we have to iterate through
        // the Truth Table from biggest to smaller
        // Ej:
        // p * q -> V F F F
        // -p -> F F V V
        // -q -> F V F V
        // p -> V V F F
        // q -> V F V F
        truth_keys.sort((t1, t2) -> t2.length() - t1.length());
        boolean flag = true;
        // The cycle stops when we compare all the truth table expressions
        for (String key : truth_keys) {
            // We can not simplify basic symbols
            if (key.length() > 2) {
                // We extract the truth table of the expression
                String current_tt = this.truth_table.get(key);
                String minor_equivalence_key = "";
                // We iterate through the other keys
                for (String inner : truth_keys) {
                    if (!inner.equals(key)) {
                        String compare_tt = this.truth_table.get(inner);
                        // If the truth tables are the same, we can put in the minor_equivalent_key
                        // variable the expression that has the equivalence
                        if (current_tt.equals(compare_tt)) {
                            minor_equivalence_key = inner;
                        }
                    }
                }
                boolean change = false;
                // If the current symbol truth table is equivalent to true or false in all
                // its rows, we proceed to change the simplified expression replacing
                // all the symbol coincidences with a V or F
                // Ej:
                // simplified_expression = q*(p*-p)
                // after substitution
                // simplified_expression = q*(F)
                if (current_tt.equals("V".repeat((int) Math.pow(2, symbols.size())))) {
                    this.simplified_expression = this.simplified_expression.replaceAll(Pattern.quote(key), "V");
                    change = true;
                }
                if (current_tt.equals("F".repeat((int) Math.pow(2, symbols.size())))) {
                    this.simplified_expression = this.simplified_expression.replaceAll(Pattern.quote(key), "F");
                    change = true;
                }
                // If we have a minor equivalent key, then we proceed to change the simplified expression replacing
                // all the symbol coincidences with the minor equivalence key
                // (p*q)+(p*q)
                // after substitution
                // p*q
                if (!(minor_equivalence_key.isEmpty() | minor_equivalence_key.isBlank())) {
                    this.simplified_expression = this.simplified_expression.replaceAll(Pattern.quote(key), minor_equivalence_key);
                    change = true;
                }
                // If we change the biggest expression in the truth table,
                // we break the cycle because we found a minor equivalent entry
                // which replace all the original expression
                if (flag && change) {
                    break;
                }
                flag = false;
            }
        }
    }

    // This method build the disjunctive canonical form
    public void canonical_disjunctive() {
        ArrayList<String> truth_keys = new ArrayList<>(this.truth_table.keySet());
        // We have to sort the list in ascending order, because we need the biggest element (final truth table)
        truth_keys.sort((t1, t2) -> t2.length() - t1.length());
        // Final truth table of the expression
        String final_truth_table = this.truth_table.get(truth_keys.get(0));
        // If the final truth table does not have true values, we can not build a disjunctive form
        if (!final_truth_table.contains("V")) {
            this.disjunctive = "It is not possible to build a disjunctive form!";
        } else {
            long count_of_positives = final_truth_table.chars().filter(ch -> ch == 'V').count();
            StringBuilder builder = new StringBuilder();
            int index_of_truth_table = 0;
            int max_index = (int) Math.pow(2, symbols.size()) - 1;
            while (index_of_truth_table <= max_index) {
                if (final_truth_table.charAt(index_of_truth_table) == 'V') {
                    builder.append("(");
                    int iter = this.symbols.size();
                    for (Character s: this.symbols.values()) {
                        String current_truth_table = this.truth_table.get(String.valueOf(s));
                        if (current_truth_table.charAt(index_of_truth_table) == 'V') {
                            builder.append(s);
                            if (iter != 1) {
                                builder.append("*");
                            }
                        } else {
                            builder.append("-").append(s);
                            if (iter != 1) {
                                builder.append("*");
                            }
                        }
                        iter--;
                    }
                    builder.append(")");
                    if (count_of_positives != 1) {
                        builder.append("+");
                    }
                    count_of_positives--;
                }
                index_of_truth_table++;
            }
            this.disjunctive = builder.toString();
        }
    }

    // This method build the conjunctive canonical form
    public void canonical_conjunctive() {
        ArrayList<String> truth_keys = new ArrayList<>(this.truth_table.keySet());
        // We have to sort the list in ascending order, because we need the biggest element (final truth table)
        truth_keys.sort((t1, t2) -> t2.length() - t1.length());
        // Final truth table of the expression
        String final_truth_table = this.truth_table.get(truth_keys.get(0));
        // If the final truth table does not have true values, we can not build a disjunctive form
        if (!final_truth_table.contains("F")) {
            this.disjunctive = "It is not possible to build a conjunctive form!";
        } else {
            long count_of_negatives = final_truth_table.chars().filter(ch -> ch == 'F').count();
            StringBuilder builder = new StringBuilder();
            int index_of_truth_table = 0;
            int max_index = (int) Math.pow(2, symbols.size()) - 1;
            while (index_of_truth_table <= max_index) {
                if (final_truth_table.charAt(index_of_truth_table) == 'F') {
                    builder.append("(");
                    int iter = this.symbols.size();
                    for (Character s: this.symbols.values()) {
                        String current_truth_table = this.truth_table.get(String.valueOf(s));
                        if (current_truth_table.charAt(index_of_truth_table) == 'F') {
                            builder.append(s);
                            if (iter != 1) {
                                builder.append("+");
                            }
                        } else {
                            builder.append("-").append(s);
                            if (iter != 1) {
                                builder.append("+");
                            }
                        }
                        iter--;
                    }
                    builder.append(")");
                    if (count_of_negatives != 1) {
                        builder.append("*");
                    }
                    count_of_negatives--;
                }
                index_of_truth_table++;
            }
            this.conjunctive = builder.toString();
        }
    }
}
