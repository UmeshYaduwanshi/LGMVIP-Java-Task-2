import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScientificCalculator extends JFrame implements ActionListener {
    private JTextField textField;
    private String input = "";

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);

        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 24));
        textField.setEditable(false);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 4, 10, 10));

        String[] buttonLabels = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
                "sin", "cos", "tan", "log",
                "<" // Backspace button
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.PLAIN, 20));
            button.addActionListener(this);
            buttonPanel.add(button);
        }

        setLayout(new BorderLayout());
        add(textField, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("=")) {
            try {
                double result = evaluateExpression(input);
                textField.setText(String.valueOf(result));
            } catch (Exception ex) {
                textField.setText("Error");
            }
            input = "";
        } else if (command.equals("sin") || command.equals("cos") || command.equals("tan") || command.equals("log")) {
            try {
                double num = Double.parseDouble(input);
                double result;
                switch (command) {
                    case "sin":
                        result = Math.sin(Math.toRadians(num));
                        break;
                    case "cos":
                        result = Math.cos(Math.toRadians(num));
                        break;
                    case "tan":
                        result = Math.tan(Math.toRadians(num));
                        break;
                    case "log":
                        result = Math.log10(num);
                        break;
                    default:
                        result = 0;
                }
                textField.setText(String.valueOf(result));
            } catch (NumberFormatException ex) {
                textField.setText("Error");
            }
            input = "";
        } else if (command.equals("<")) {
            if (!input.isEmpty()) {
                input = input.substring(0, input.length() - 1);
                textField.setText(input);
            }
        } else {
            input += command;
            textField.setText(input);
        }
    }

    private double evaluateExpression(String expression) {
        try {
            return new CalculatorEngine().evaluate(expression);
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating expression");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ScientificCalculator().setVisible(true));
    }
}

class CalculatorEngine {
    public double evaluate(String expression) {
        try {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (; ; ) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (; ; ) {
                        if (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    if (eat('^')) x = Math.pow(x, parseFactor());

                    return x;
                }
            }.parse();
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating expression");
        }
    }
}
