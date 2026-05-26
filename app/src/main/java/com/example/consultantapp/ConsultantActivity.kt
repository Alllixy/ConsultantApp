package com.example.consultantapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/*** Панель управления для консультанта*/
class ConsultantActivity : AppCompatActivity() {

    private var userId: Int = 0
    private lateinit var mainLayout: LinearLayout
    private lateinit var container: LinearLayout
    private lateinit var loadingPanel: LinearLayout
    private lateinit var tabToday: TextView
    private lateinit var tabWeek: TextView
    private var currentView: String = "today"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = intent.getIntExtra("USER_ID", 8)

        mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A5276"))
            setPadding(0, 0, 0, 0)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 20)
            setBackgroundColor(Color.WHITE)
            elevation = 4f
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "Панель консультанта"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#2C3E50"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val logoutButton = TextView(this).apply {
            text = "Выйти"
            textSize = 14f
            setTextColor(Color.parseColor("#E74C3C"))
            setPadding(20, 8, 0, 8)
            setOnClickListener {
                getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this@ConsultantActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()
            }
        }

        topRow.addView(title)
        topRow.addView(logoutButton)
        header.addView(topRow)

        val tabLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 20, 40, 0)
            setBackgroundColor(Color.WHITE)
        }

        tabToday = TextView(this).apply {
            text = "Сегодня"
            textSize = 16f
            setTextColor(Color.parseColor("#3498DB"))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 15, 0, 15)
            setOnClickListener { switchToToday() }
        }

        tabWeek = TextView(this).apply {
            text = "Неделя"
            textSize = 16f
            setTextColor(Color.parseColor("#7F8C8D"))
            setTypeface(null, Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 15, 0, 15)
            setOnClickListener { switchToWeek() }
        }

        val tabUnderline = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            )
            setBackgroundColor(Color.parseColor("#3498DB"))
        }

        tabLayout.addView(tabToday)
        tabLayout.addView(tabWeek)

        loadingPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 100, 0, 0)
            visibility = View.GONE
        }
        val progressBar = ProgressBar(this)
        val loadingText = TextView(this).apply {
            text = "Загрузка..."
            textSize = 14f
            setTextColor(Color.parseColor("#7F8C8D"))
            setPadding(0, 20, 0, 0)
        }
        loadingPanel.addView(progressBar)
        loadingPanel.addView(loadingText)

        val scrollView = ScrollView(this)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 20, 30, 100)
        }
        scrollView.addView(container)

        mainLayout.addView(header)
        mainLayout.addView(tabLayout)
        mainLayout.addView(tabUnderline)
        mainLayout.addView(loadingPanel)
        mainLayout.addView(scrollView)

        setContentView(mainLayout)

        loadTodayAppointments()
    }

    /** Переключение на вкладку "Сегодня" */
    private fun switchToToday() {
        currentView = "today"
        tabToday.setTextColor(Color.parseColor("#3498DB"))
        tabToday.setTypeface(null, Typeface.BOLD)
        tabWeek.setTextColor(Color.parseColor("#7F8C8D"))
        tabWeek.setTypeface(null, Typeface.NORMAL)
        loadTodayAppointments()
    }

    /** Переключение на вкладку "Неделя" */
    private fun switchToWeek() {
        currentView = "week"
        tabWeek.setTextColor(Color.parseColor("#3498DB"))
        tabWeek.setTypeface(null, Typeface.BOLD)
        tabToday.setTextColor(Color.parseColor("#7F8C8D"))
        tabToday.setTypeface(null, Typeface.NORMAL)
        loadWeekAppointments()
    }

    /** Загрузка записей на сегодня */
    private fun loadTodayAppointments() {
        showLoading(true)
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/today-appointments")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONArray(response)

                val filteredJson = JSONArray()
                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)
                    if (obj.getInt("ConsultantID") == userId) {
                        filteredJson.put(obj)
                    }
                }

                runOnUiThread {
                    showLoading(false)
                    displayAppointments(filteredJson)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    showError(e.message)
                }
            }
        }
    }

    /** Загрузка записей на неделю  */
    private fun loadWeekAppointments() {
        showLoading(true)
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/week-appointments")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONArray(response)

                val filteredJson = JSONArray()
                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)
                    if (obj.getInt("ConsultantID") == userId) {
                        filteredJson.put(obj)
                    }
                }

                runOnUiThread {
                    showLoading(false)
                    displayAppointments(filteredJson)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    showError(e.message)
                }
            }
        }
    }

    /** Отображение списка записей */
    private fun displayAppointments(json: JSONArray) {
        container.removeAllViews()
        if (json.length() == 0) {
            val emptyText = TextView(this).apply {
                text = "Нет записей"
                textSize = 14f
                setTextColor(Color.parseColor("#7F8C8D"))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 50, 0, 0)
            }
            container.addView(emptyText)
            return
        }

        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val appointmentId = obj.getInt("AppointmentID")
            val clientName = obj.getString("ClientName")
            val consultantName = obj.getString("ConsultantName")
            val serviceName = obj.getString("ServiceName")
            val date = obj.getString("AppointmentDate")
            val time = obj.getString("StartTime")
            val status = obj.getString("Status")

            container.addView(createAppointmentCard(appointmentId, clientName, consultantName, serviceName, date, time, status))
        }
    }

    /** Создание карточки записи для консультанта  */
    private fun createAppointmentCard(appointmentId: Int, clientName: String, consultantName: String, serviceName: String, date: String, time: String, status: String): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 15) }
            setPadding(30, 25, 30, 20)
            setBackgroundColor(Color.WHITE)
        }

        val drawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 16f
            setStroke(1, Color.parseColor("#E0E0E0"))
        }
        card.background = drawable

        val formattedDate = try {
            date.split("T")[0].split("-").reversed().joinToString(".")
        } catch (e: Exception) {
            date
        }

        val formattedTime = try {
            time.split("T")[1].split(".")[0].substring(0, 5)
        } catch (e: Exception) {
            time
        }

        val clientText = TextView(this).apply {
            text = "Клиент: $clientName"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#2C3E50"))
            setPadding(0, 0, 0, 8)
        }
        card.addView(clientText)

        val serviceText = TextView(this).apply {
            text = "Услуга: $serviceName"
            textSize = 14f
            setTextColor(Color.parseColor("#7F8C8D"))
            setPadding(0, 0, 0, 8)
        }
        card.addView(serviceText)

        val dateText = TextView(this).apply {
            text = "$formattedDate в $formattedTime"
            textSize = 13f
            setTextColor(Color.parseColor("#3498DB"))
            setPadding(0, 0, 0, 8)
        }
        card.addView(dateText)

        val statusText = TextView(this).apply {
            text = when (status) {
                "Подтверждена" -> "Подтверждена"
                "В ожидании" -> "В ожидании"
                "Отменена" -> "Отменена"
                "Выполнена" -> "Выполнена"
                "Отклонена" -> "Отклонена"
                else -> status
            }
            textSize = 13f
            val color = when (status) {
                "Подтверждена" -> Color.parseColor("#27AE60")
                "В ожидании" -> Color.parseColor("#F39C12")
                "Отменена" -> Color.parseColor("#E74C3C")
                "Выполнена" -> Color.parseColor("#95A5A6")
                "Отклонена" -> Color.parseColor("#E74C3C")
                else -> Color.parseColor("#7F8C8D")
            }
            setTextColor(color)
            setPadding(0, 0, 0, 12)
        }
        card.addView(statusText)

        return card
    }

    /** Отображение панели загрузки */
    private fun showLoading(show: Boolean) {
        loadingPanel.visibility = if (show) View.VISIBLE else View.GONE
    }

    /** Отображение сообщения об ошибке */
    private fun showError(message: String?) {
        val errorText = TextView(this).apply {
            text = "Ошибка: ${message ?: "неизвестная ошибка"}"
            textSize = 14f
            setTextColor(Color.RED)
            setPadding(30, 50, 30, 50)
        }
        container.addView(errorText)
    }
}