package com.example.consultantapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var statusText: TextView
    private lateinit var loadingPanel: LinearLayout
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedRole = prefs.getString("user_role", "") ?: ""
        val savedUserId = prefs.getInt("user_id", 0)
        if (savedRole.isNotEmpty() && savedUserId != 0) {
            openMainActivity(savedRole, savedUserId)
            return
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A5276"))
            setPadding(0, 0, 0, 0)
        }

        val topSpacer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val bottomSpacer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.WHITE)
            setPadding(40, 80, 40, 30)
        }

        val title = TextView(this).apply {
            text = "Консультант"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }
        headerContainer.addView(title)

        val cardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(30, 30, 30, 0)
            layoutParams = params
            setBackgroundColor(Color.WHITE)
            setPadding(30, 40, 30, 30)
        }

        val cardDrawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 24f
        }
        cardContainer.background = cardDrawable

        val formTitle = TextView(this).apply {
            text = "Вход в аккаунт"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 30)
            gravity = android.view.Gravity.CENTER
        }
        cardContainer.addView(formTitle)

        val titleSpacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40
            )
        }
        cardContainer.addView(titleSpacer)

        emailInput = EditText(this).apply {
            hint = "Email / Телефон"
            textSize = 16f
            setPadding(25, 20, 25, 20)
            background = createEditTextBackground()
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        }
        cardContainer.addView(emailInput)

        val passwordContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            background = createEditTextBackground()
        }

        passwordInput = EditText(this).apply {
            hint = "Пароль"
            textSize = 16f
            setPadding(25, 20, 25, 20)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            transformationMethod = PasswordTransformationMethod.getInstance()
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            background = null
            gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
        }

        val eyeButton = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_view)
            setPadding(0, 20, 25, 20)
            setColorFilter(Color.parseColor("#7F8C8D"))
            setOnClickListener {
                isPasswordVisible = !isPasswordVisible
                if (isPasswordVisible) {
                    passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    passwordInput.transformationMethod = null
                    setColorFilter(Color.parseColor("#3498DB"))
                } else {
                    passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                    setColorFilter(Color.parseColor("#7F8C8D"))
                }
                passwordInput.setSelection(passwordInput.text.length)
            }
        }

        passwordContainer.addView(passwordInput)
        passwordContainer.addView(eyeButton)
        cardContainer.addView(passwordContainer)

        val buttonSpacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40
            )
        }
        cardContainer.addView(buttonSpacer)

        val loginButton = Button(this).apply {
            text = "Войти"
            textSize = 16f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { performLogin() }
        }

        val buttonDrawable = GradientDrawable().apply {
            setColor(Color.parseColor("#1A5276"))
            cornerRadius = 12f
        }
        loginButton.background = buttonDrawable
        cardContainer.addView(loginButton)

        val registerButton = TextView(this).apply {
            text = "Зарегистрироваться"
            textSize = 15f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 30, 0, 30)
            setOnClickListener { showRegistrationDialog() }
        }

        val statusContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        statusText = TextView(this).apply {
            text = ""
            textSize = 14f
            setTextColor(Color.parseColor("#FFCDD2"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 10, 0, 0)
        }

        loadingPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            visibility = View.GONE
        }
        val progressBar = ProgressBar(this)
        loadingPanel.addView(progressBar)

        statusContainer.addView(statusText)
        statusContainer.addView(loadingPanel)

        rootLayout.addView(headerContainer)
        rootLayout.addView(topSpacer)
        rootLayout.addView(cardContainer)
        rootLayout.addView(bottomSpacer)
        rootLayout.addView(registerButton)
        rootLayout.addView(statusContainer)

        setContentView(rootLayout)
    }

    /** Создание фона для полей ввода */
    private fun createEditTextBackground(): android.graphics.drawable.Drawable {
        val drawable = GradientDrawable()
        drawable.setColor(Color.parseColor("#F2F2F2"))
        drawable.cornerRadius = 12f
        drawable.setStroke(1, Color.parseColor("#E0E0E0"))
        return drawable
    }

    /** Проверка логина и пароля, открытие соответствующей активности */
    private fun performLogin() {
        val login = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (login.isEmpty() || password.isEmpty()) {
            statusText.text = "Заполните все поля"
            return
        }

        showLoading(true)

        thread {
            Thread.sleep(500)

            val user = when {
                login == "anna@mail.ru" && password == "123" -> Triple("Клиент", 1, "Анна Смирнова")
                login == "dmitry@mail.ru" && password == "123" -> Triple("Клиент", 2, "Дмитрий Иванов")
                login == "ivan.consultant@consult.ru" && password == "123" -> Triple("Консультант", 8, "Иван Петров")
                login == "maria.consultant@consult.ru" && password == "123" -> Triple("Консультант", 9, "Мария Соколова")
                login == "admin@consult.ru" && password == "admin123" -> Triple("Администратор", 12, "Александр Алексеев")
                else -> null
            }

            runOnUiThread {
                showLoading(false)
                if (user != null) {
                    val (role, id, name) = user
                    val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("user_role", role).apply()
                    prefs.edit().putInt("user_id", id).apply()
                    prefs.edit().putString("user_name", name).apply()

                    Toast.makeText(this@LoginActivity, "Добро пожаловать, $name!", Toast.LENGTH_SHORT).show()
                    openMainActivity(role, id)
                } else {
                    statusText.text = "Неверный email/телефон или пароль"
                }
            }
        }
    }

    /** Отображение диалога регистрации нового пользователя */
    private fun showRegistrationDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 30)
            setBackgroundColor(Color.WHITE)
        }

        val dialogDrawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 24f
        }
        dialogLayout.background = dialogDrawable

        val title = TextView(this).apply {
            text = "Регистрация"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 10, 0, 30)
        }
        dialogLayout.addView(title)

        val lastNameInput = EditText(this).apply {
            hint = "Фамилия"
            textSize = 16f
            setPadding(25, 20, 25, 20)
            background = createEditTextBackground()
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        }
        dialogLayout.addView(lastNameInput)

        val firstNameInput = EditText(this).apply {
            hint = "Имя"
            textSize = 16f
            setPadding(25, 20, 25, 20)
            background = createEditTextBackground()
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        }
        dialogLayout.addView(firstNameInput)

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            setPadding(0, 10, 0, 20)
        }
        val radioEmail = RadioButton(this).apply {
            text = "По Email"
            setTextColor(Color.BLACK)
        }
        val radioPhone = RadioButton(this).apply {
            text = "По телефону"
            setTextColor(Color.BLACK)
        }
        radioGroup.addView(radioEmail)
        radioGroup.addView(radioPhone)
        radioEmail.isChecked = true
        dialogLayout.addView(radioGroup)

        val emailInputReg = EditText(this).apply {
            hint = "Email"
            textSize = 16f
            setPadding(25, 20, 25, 20)
            background = createEditTextBackground()
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            visibility = View.VISIBLE
        }
        dialogLayout.addView(emailInputReg)

        val phoneInputReg = EditText(this).apply {
            hint = "Телефон"
            textSize = 16f
            setPadding(25, 20, 25, 20)
            background = createEditTextBackground()
            inputType = InputType.TYPE_CLASS_PHONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            visibility = View.GONE
        }
        dialogLayout.addView(phoneInputReg)

        val passwordInputReg = EditText(this).apply {
            hint = "Пароль"
            textSize = 16f
            setPadding(25, 20, 25, 20)
            background = createEditTextBackground()
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        }
        dialogLayout.addView(passwordInputReg)

        val loadingBar = ProgressBar(this).apply { visibility = View.GONE }
        dialogLayout.addView(loadingBar)

        val submitButton = Button(this).apply {
            text = "Зарегистрироваться"
            textSize = 16f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 20, 0, 0) }
        }
        val buttonDrawable = GradientDrawable().apply {
            setColor(Color.parseColor("#1A5276"))
            cornerRadius = 12f
        }
        submitButton.background = buttonDrawable
        dialogLayout.addView(submitButton)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                radioEmail.id -> {
                    emailInputReg.visibility = View.VISIBLE
                    phoneInputReg.visibility = View.GONE
                }
                radioPhone.id -> {
                    emailInputReg.visibility = View.GONE
                    phoneInputReg.visibility = View.VISIBLE
                }
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setCancelable(true)
            .create()

        submitButton.setOnClickListener {
            val lastName = lastNameInput.text.toString().trim()
            val firstName = firstNameInput.text.toString().trim()
            val fullName = "$lastName $firstName".trim()
            val email = if (radioEmail.isChecked) emailInputReg.text.toString().trim() else ""
            val phone = if (radioPhone.isChecked) phoneInputReg.text.toString().trim() else ""
            val password = passwordInputReg.text.toString()

            if (lastName.isEmpty() || firstName.isEmpty()) {
                Toast.makeText(this, "Заполните фамилию и имя", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (radioEmail.isChecked && email.isEmpty()) {
                Toast.makeText(this, "Введите Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (radioPhone.isChecked && phone.isEmpty()) {
                Toast.makeText(this, "Введите телефон", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitButton.isEnabled = false
            loadingBar.visibility = View.VISIBLE

            thread {
                try {
                    val url = URL("http://10.0.2.2:3000/api/register")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    val jsonBody = JSONObject().apply {
                        put("FullName", fullName)
                        put("Password", password)
                        put("Role", "Клиент")
                        if (radioEmail.isChecked) {
                            put("Email", email)
                            put("PhoneNumber", "")
                        } else {
                            put("Email", "")
                            put("PhoneNumber", phone)
                        }
                    }

                    conn.outputStream.write(jsonBody.toString().toByteArray())

                    val response = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)

                    runOnUiThread {
                        submitButton.isEnabled = true
                        loadingBar.visibility = View.GONE

                        if (json.getBoolean("success")) {
                            Toast.makeText(this@LoginActivity, "Регистрация успешна!", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        } else {
                            val message = json.optString("message", "Ошибка регистрации")
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        submitButton.isEnabled = true
                        loadingBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

    /** Открытие главной активности в зависимости от роли пользователя */
    private fun openMainActivity(role: String, userId: Int) {
        val intent = when (role) {
            "Клиент" -> Intent(this, MainActivity::class.java)
            "Консультант" -> Intent(this, ConsultantActivity::class.java)
            else -> Intent(this, AdminActivity::class.java)
        }
        intent.putExtra("USER_ROLE", role)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }

    /** Отображение/скрытие панели загрузки */
    private fun showLoading(show: Boolean) {
        loadingPanel.visibility = if (show) View.VISIBLE else View.GONE
        emailInput.isEnabled = !show
        passwordInput.isEnabled = !show
    }
}