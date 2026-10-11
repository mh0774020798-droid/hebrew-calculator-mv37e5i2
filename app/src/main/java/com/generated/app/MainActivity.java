package com.generated.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final String PREFS_NAME = "calc_history_prefs";
    private static final String KEY_HISTORY = "history_entries";

    private TextView tvExpression;
    private TextView tvResult;
    private Button btnClear;

    private String currentInput = "0";
    private String operand1 = null;
    private String pendingOp = null;
    private boolean isResultCalculated = false;

    private final ArrayList<String> historyList = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadHistory();

        tvExpression = findViewById(R.id.tv_expression);
        tvResult = findViewById(R.id.tv_result);
        btnClear = findViewById(R.id.btn_clear);

        setupNumberButtons();
        setupOperationButtons();
        setupUtilityButtons();
        updateDisplay();
    }

    private void setupNumberButtons() {
        int[] numButtonIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };

        for (int id : numButtonIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    appendDigit(b.getText().toString());
                }
            });
        }

        findViewById(R.id.btn_dot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendDot();
            }
        });
    }

    private void setupOperationButtons() {
        findViewById(R.id.btn_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOperator("+");
            }
        });

        findViewById(R.id.btn_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOperator("-");
            }
        });

        findViewById(R.id.btn_multiply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOperator("×");
            }
        });

        findViewById(R.id.btn_divide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOperator("÷");
            }
        });

        findViewById(R.id.btn_equals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateResult();
            }
        });

        findViewById(R.id.btn_percent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePercent();
            }
        });

        findViewById(R.id.btn_negate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNegate();
            }
        });
    }

    private void setupUtilityButtons() {
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });

        findViewById(R.id.btn_backspace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackspace();
            }
        });

        ImageButton btnHistory = findViewById(R.id.btn_history);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryDialog();
            }
        });
    }

    private void appendDigit(String digit) {
        if (isResultCalculated) {
            currentInput = digit;
            isResultCalculated = false;
        } else {
            if ("0".equals(currentInput)) {
                currentInput = digit;
            } else {
                if (currentInput.replace(".", "").replace("-", "").length() < 15) {
                    currentInput += digit;
                }
            }
        }
        updateDisplay();
    }

    private void appendDot() {
        if (isResultCalculated) {
            currentInput = "0.";
            isResultCalculated = false;
        } else {
            if (!currentInput.contains(".")) {
                currentInput += ".";
            }
        }
        updateDisplay();
    }

    private void handleOperator(String op) {
        if (operand1 != null && pendingOp != null && !isResultCalculated) {
            // Chain calculation
            String calcRes = evaluate(operand1, currentInput, pendingOp);
            if (calcRes == null) {
                showError(getString(R.string.error_divide_zero));
                return;
            }
            operand1 = calcRes;
            tvResult.setText(formatResult(operand1));
        } else {
            operand1 = currentInput;
        }

        pendingOp = op;
        isResultCalculated = true;
        updateDisplay();
    }

    private void handlePercent() {
        try {
            BigDecimal val = new BigDecimal(currentInput);
            if (operand1 != null && pendingOp != null) {
                // Percentage of operand1 (e.g., 200 + 10% = 200 + 20)
                BigDecimal base = new BigDecimal(operand1);
                val = base.multiply(val).divide(new BigDecimal("100"), MathContext.DECIMAL64);
            } else {
                val = val.divide(new BigDecimal("100"), MathContext.DECIMAL64);
            }
            currentInput = stripTrailingZeros(val);
            isResultCalculated = false;
            updateDisplay();
        } catch (Exception ignored) {
        }
    }

    private void handleNegate() {
        if ("0".equals(currentInput) || "0.0".equals(currentInput)) {
            return;
        }
        if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1);
        } else {
            currentInput = "-" + currentInput;
        }
        updateDisplay();
    }

    private void handleBackspace() {
        if (isResultCalculated) {
            clearAll();
            return;
        }
        if (currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if ("-".equals(currentInput)) {
                currentInput = "0";
            }
        } else {
            currentInput = "0";
        }
        updateDisplay();
    }

    private void clearAll() {
        currentInput = "0";
        operand1 = null;
        pendingOp = null;
        isResultCalculated = false;
        tvExpression.setText("");
        updateDisplay();
    }

    private void calculateResult() {
        if (operand1 != null && pendingOp != null) {
            String exprText = operand1 + " " + pendingOp + " " + currentInput;
            String resultStr = evaluate(operand1, currentInput, pendingOp);

            if (resultStr == null) {
                showError(getString(R.string.error_divide_zero));
                return;
            }

            addHistory(exprText + " = " + formatResult(resultStr));
            tvExpression.setText(exprText + " =");
            currentInput = resultStr;
            operand1 = null;
            pendingOp = null;
            isResultCalculated = true;
            tvResult.setText(formatResult(currentInput));
        }
    }

    private String evaluate(String op1Str, String op2Str, String op) {
        try {
            BigDecimal a = new BigDecimal(op1Str);
            BigDecimal b = new BigDecimal(op2Str);
            BigDecimal res;

            switch (op) {
                case "+":
                    res = a.add(b, MathContext.DECIMAL64);
                    break;
                case "-":
                    res = a.subtract(b, MathContext.DECIMAL64);
                    break;
                case "×":
                    res = a.multiply(b, MathContext.DECIMAL64);
                    break;
                case "÷":
                    if (b.compareTo(BigDecimal.ZERO) == 0) {
                        return null; // Division by zero
                    }
                    res = a.divide(b, 10, RoundingMode.HALF_UP);
                    break;
                default:
                    return op2Str;
            }
            return stripTrailingZeros(res);
        } catch (Exception e) {
            return "0";
        }
    }

    private String stripTrailingZeros(BigDecimal bd) {
        bd = bd.stripTrailingZeros();
        String str = bd.toPlainString();
        if (str.contains(".") && str.length() > 14) {
            // round slightly to prevent overflow
            bd = bd.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros();
            str = bd.toPlainString();
        }
        return str;
    }

    private void updateDisplay() {
        if (operand1 != null && pendingOp != null) {
            tvExpression.setText(formatResult(operand1) + " " + pendingOp);
        } else if (!isResultCalculated) {
            tvExpression.setText("");
        }
        tvResult.setText(formatResult(currentInput));
    }

    private String formatResult(String numStr) {
        if (numStr == null || numStr.isEmpty()) return "0";
        if (numStr.endsWith(".")) return numStr;
        try {
            // If already standard notation
            return numStr;
        } catch (Exception e) {
            return numStr;
        }
    }

    private void showError(String message) {
        currentInput = "0";
        operand1 = null;
        pendingOp = null;
        isResultCalculated = true;
        tvExpression.setText("");
        tvResult.setText(getString(R.string.error_invalid_format));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loadHistory() {
        historyList.clear();
        String saved = prefs.getString(KEY_HISTORY, "");
        if (!saved.isEmpty()) {
            String[] items = saved.split(";;;");
            historyList.addAll(Arrays.asList(items));
        }
    }

    private void saveHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < historyList.size(); i++) {
            if (i > 0) sb.append(";;;");
            sb.append(historyList.get(i));
        }
        prefs.edit().putString(KEY_HISTORY, sb.toString()).apply();
    }

    private void addHistory(String entry) {
        historyList.add(0, entry);
        if (historyList.size() > 50) {
            historyList.remove(historyList.size() - 1);
        }
        saveHistory();
    }

    private void showHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.history_title));

        if (historyList.isEmpty()) {
            builder.setMessage(getString(R.string.history_empty));
            builder.setPositiveButton(getString(R.string.close), null);
        } else {
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, new ArrayList<>(historyList)) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    tv.setTextSize(16f);
                    tv.setTextColor(getResources().getColor(R.color.text_primary));
                    tv.setPadding(24, 20, 24, 20);
                    return view;
                }
            };

            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Tap on history item to paste result
                    String item = historyList.get(which);
                    String[] parts = item.split(" = ");
                    if (parts.length == 2) {
                        currentInput = parts[1].trim();
                        operand1 = null;
                        pendingOp = null;
                        isResultCalculated = true;
                        updateDisplay();
                    }
                }
            });

            builder.setNegativeButton(getString(R.string.clear_history), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    historyList.clear();
                    saveHistory();
                    Toast.makeText(MainActivity.this, getString(R.string.clear_history), Toast.LENGTH_SHORT).show();
                }
            });

            builder.setPositiveButton(getString(R.string.close), null);
        }

        builder.show();
    }
}
