package com.mtm.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mtm.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentInput: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun setupSystemBars() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = ContextCompat.getColor(this, R.color.off_white),
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
    }

    private fun initListeners() {
        setupClearButton()
        setupBackspaceButton()
        setupEqualButton()
    }

    private fun setupClearButton() {
        binding.btnAc.setOnClickListener {
            resetCalculator()
        }
    }

    private fun setupBackspaceButton() {
        binding.btnBack.setOnClickListener {
            val input = binding.txOperation.text.toString()

            binding.txOperation.text = when {
                input.isEmpty() || input == "0" -> "0"
                input.length == 1 -> "0"
                else -> input.dropLast(1)
            }
            adjustTextSize(binding.txOperation)
        }
    }

    private fun setupEqualButton() {
        binding.btnEqual.setOnClickListener {
            try {
                val expression = binding.txOperation.text.toString()

                if (isExpressionValid(expression)) {
                    if (hasOperator(expression)) {
                        val shuntingYard = ShuntingYardAlgorithm()
                        val postfix = shuntingYard.toPostfix(expression)
                        val result = shuntingYard.evaluatePostfix(postfix)

                        updateResult(expression, result)
                    } else {
                        updateResult(expression, expression)
                    }
                } else {
                    showError()
                }
            } catch (e: Exception) {
                showError()
            }
        }
    }

    private fun updateResult(prevExpression: String, result: String) {
        binding.txPrevOperation.text = prevExpression
        binding.txOperation.text = result
        binding.txError.visibility = View.INVISIBLE

        adjustTextSize(binding.txOperation)
    }

    private fun resetCalculator() {
        binding.txPrevOperation.text = ""
        binding.txOperation.text = "0"
        binding.txError.visibility = View.INVISIBLE

        adjustTextSize(binding.txOperation)
    }

    private fun showError() {
        binding.txOperation.text = ""
        binding.txPrevOperation.text = ""
        binding.txError.visibility = View.VISIBLE
    }

    private fun hasOperator(expression: String): Boolean {
        return expression.contains(Regex("[+\\-x/%]"))
    }

    private fun isExpressionValid(expression: String): Boolean {
        return expression.isNotEmpty() && !expression.endsWith("+-x/%.")
    }

    fun onNumberClick(view: View) {
        setClickAnimation(view)
        val newDigit = prepareClickAction(view)

        binding.txOperation.text =
            if (currentInput == "0") newDigit else currentInput + newDigit
    }

    fun onOperationClick(view: View) {
        setClickAnimation(view)
        val newOperator = prepareClickAction(view)

        binding.txOperation.text = when {
            isLastCharDigit() -> currentInput + newOperator
            currentInput.isNotEmpty() -> currentInput.dropLast(1) + newOperator
            else -> currentInput
        }
    }

    fun onDotClick(view: View) {
        val newDigit = prepareClickAction(view)
        val parts = currentInput.split(Regex("[+\\-x/%]"))
        val lastPart = parts.lastOrNull() ?: ""

        if (!lastPart.contains(".")) {
            binding.txOperation.text =
                if (isLastCharDigit()) currentInput + newDigit
                else currentInput + "0$newDigit"
        }
    }

    private fun setClickAnimation(view: View) {
        view.animate()
            .scaleX(0.90f)
            .scaleY(0.90f)
            .setDuration(200)
            .withEndAction { view.animate().scaleX(1f).scaleY(1f).duration = 200 }
    }

    private fun prepareClickAction(view: View): String {
        binding.txError.visibility = View.INVISIBLE
        val input = (view as Button).text.toString()
        currentInput = binding.txOperation.text.toString()
        adjustTextSize(binding.txOperation)
        return input
    }

    private fun isLastCharDigit(): Boolean {
        return currentInput.isNotEmpty() && currentInput.last().isDigit()
    }

    private fun adjustTextSize(textView: TextView) {
        val textLength = textView.text.toString().length
        val textSize = when {
            textLength <= 10 -> 36f
            textLength <= 15 -> 32f
            textLength <= 20 -> 28f
            textLength <= 25 -> 24f
            else -> 20f
        }
        textView.textSize = textSize
    }
}