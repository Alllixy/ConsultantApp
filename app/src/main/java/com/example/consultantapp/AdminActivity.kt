package com.example.consultantapp

import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

/**
 * Панель управления для администратора
 */
class AdminActivity : AppCompatActivity() {

    private var userId: Int = 0
    private lateinit var mainLayout: LinearLayout
    private lateinit var container: LinearLayout
    private lateinit var loadingPanel: LinearLayout
    private lateinit var tabToday: TextView
    private lateinit var tabWeek: TextView
    private lateinit var tabSchedule: TextView
    private lateinit var consultantSpinner: Spinner
    private var currentView: String = "today"
    private var selectedConsultantId: Int = 0
    private val consultantsList = mutableListOf<Pair<Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = intent.getIntExtra("USER_ID", 12)

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
            text = "Панель администратора"
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
                val intent = Intent(this@AdminActivity, LoginActivity::class.java)
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

        tabSchedule = TextView(this).apply {
            text = "Расписание"
            textSize = 16f
            setTextColor(Color.parseColor("#7F8C8D"))
            setTypeface(null, Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 15, 0, 15)
            setOnClickListener { switchToSchedule() }
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
        tabLayout.addView(tabSchedule)

        val filterContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 20, 30, 20)
            setBackgroundColor(Color.WHITE)
            visibility = View.VISIBLE
        }

        val filterLabel = TextView(this).apply {
            text = "Фильтр по консультанту"
            textSize = 14f
            setTextColor(Color.parseColor("#7F8C8D"))
            setPadding(0, 0, 0, 8)
        }
        filterContainer.addView(filterLabel)

        consultantSpinner = Spinner(this)
        filterContainer.addView(consultantSpinner)

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
        mainLayout.addView(filterContainer)
        mainLayout.addView(loadingPanel)
        mainLayout.addView(scrollView)

        setContentView(mainLayout)

        loadConsultants()
    }

    /** Загрузка списка консультантов для фильтра */
    private fun loadConsultants() {
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/consultants")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONArray(response)

                consultantsList.clear()
                consultantsList.add(Pair(0, "Все консультанты"))
                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)
                    consultantsList.add(Pair(obj.getInt("UserID"), obj.getString("FullName")))
                }

                runOnUiThread {
                    val adapter = ArrayAdapter(this@AdminActivity, android.R.layout.simple_spinner_item, consultantsList.map { it.second })
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    consultantSpinner.adapter = adapter

                    consultantSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedConsultantId = consultantsList[position].first
                            if (currentView == "today") loadTodayAppointments()
                            else if (currentView == "week") loadWeekAppointments()
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    loadTodayAppointments()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError(e.message)
                }
            }
        }
    }

    /** Переключение на вкладку "Сегодня" */
    private fun switchToToday() {
        currentView = "today"
        tabToday.setTextColor(Color.parseColor("#3498DB"))
        tabToday.setTypeface(null, Typeface.BOLD)
        tabWeek.setTextColor(Color.parseColor("#7F8C8D"))
        tabWeek.setTypeface(null, Typeface.NORMAL)
        tabSchedule.setTextColor(Color.parseColor("#7F8C8D"))
        tabSchedule.setTypeface(null, Typeface.NORMAL)
        loadTodayAppointments()
    }

    /** Переключение на вкладку "Неделя" */
    private fun switchToWeek() {
        currentView = "week"
        tabWeek.setTextColor(Color.parseColor("#3498DB"))
        tabWeek.setTypeface(null, Typeface.BOLD)
        tabToday.setTextColor(Color.parseColor("#7F8C8D"))
        tabToday.setTypeface(null, Typeface.NORMAL)
        tabSchedule.setTextColor(Color.parseColor("#7F8C8D"))
        tabSchedule.setTypeface(null, Typeface.NORMAL)
        loadWeekAppointments()
    }

    /** Переключение на вкладку "Расписание" */
    private fun switchToSchedule() {
        currentView = "schedule"
        tabSchedule.setTextColor(Color.parseColor("#3498DB"))
        tabSchedule.setTypeface(null, Typeface.BOLD)
        tabToday.setTextColor(Color.parseColor("#7F8C8D"))
        tabToday.setTypeface(null, Typeface.NORMAL)
        tabWeek.setTextColor(Color.parseColor("#7F8C8D"))
        tabWeek.setTypeface(null, Typeface.NORMAL)
        loadSchedule()
    }

    /** Загрузка и отображение расписания всех консультантов */
    private fun loadSchedule() {
        showLoading(true)
        container.removeAllViews()

        if (consultantsList.size <= 1) {
            runOnUiThread {
                showLoading(false)
                val waitText = TextView(this).apply {
                    text = "Загрузка списка консультантов..."
                    textSize = 14f
                    setTextColor(Color.parseColor("#7F8C8D"))
                    gravity = android.view.Gravity.CENTER
                    setPadding(0, 50, 0, 0)
                }
                container.addView(waitText)
            }
            return
        }

        var loadedCount = 0
        val totalCount = consultantsList.size - 1

        for (i in 1 until consultantsList.size) {
            val consultant = consultantsList[i]
            val consultantId = consultant.first
            val consultantName = consultant.second

            thread {
                try {
                    val url = URL("http://10.0.2.2:3000/api/schedule/$consultantId")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 5000

                    val response = conn.inputStream.bufferedReader().readText()
                    val json = JSONArray(response)

                    val scheduleMap = mutableMapOf<Int, Pair<String?, String?>>()
                    for (j in 0 until json.length()) {
                        val obj = json.getJSONObject(j)
                        val weekDay = obj.getInt("WeekDay")
                        var startTime = obj.optString("StartTime", null)
                        var endTime = obj.optString("EndTime", null)

                        if (startTime != null && startTime.contains("T")) {
                            startTime = startTime.substring(11, 16)
                        }
                        if (endTime != null && endTime.contains("T")) {
                            endTime = endTime.substring(11, 16)
                        }

                        scheduleMap[weekDay] = Pair(startTime, endTime)
                    }

                    runOnUiThread {
                        val card = createScheduleCard(consultantId, consultantName, scheduleMap)
                        container.addView(card)
                        loadedCount++
                        if (loadedCount == totalCount) {
                            showLoading(false)
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        val errorText = TextView(this).apply {
                            text = "Ошибка загрузки расписания для $consultantName"
                            textSize = 14f
                            setTextColor(Color.RED)
                            setPadding(30, 25, 30, 25)
                        }
                        container.addView(errorText)
                        loadedCount++
                        if (loadedCount == totalCount) {
                            showLoading(false)
                        }
                    }
                }
            }
        }
    }

    /** Создание карточки расписания для одного консультанта */
    private fun createScheduleCard(consultantId: Int, consultantName: String, scheduleMap: Map<Int, Pair<String?, String?>>): LinearLayout {
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

        val nameText = TextView(this).apply {
            text = consultantName
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#2C3E50"))
            setPadding(0, 0, 0, 15)
        }
        card.addView(nameText)

        val days = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")

        for (i in 0 until days.size) {
            val dayOfWeek = i + 1
            val times = scheduleMap[dayOfWeek]
            val startTime = times?.first
            val endTime = times?.second

            val dayLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 8, 0, 8)
                gravity = android.view.Gravity.CENTER
            }

            val dayText = TextView(this).apply {
                text = days[i]
                textSize = 14f
                setTextColor(Color.parseColor("#2C3E50"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f)
            }

            val timeText = TextView(this).apply {
                if (startTime != null && endTime != null && startTime != "null" && endTime != "null") {
                    text = "$startTime - $endTime"
                } else {
                    text = "Выходной"
                }
                textSize = 13f
                setTextColor(Color.parseColor("#000000"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f)
                gravity = android.view.Gravity.CENTER
            }

            val editButton = Button(this).apply {
                text = "Изменить"
                textSize = 12f
                setBackgroundColor(Color.parseColor("#3498DB"))
                setTextColor(Color.WHITE)
                setPadding(12, 8, 12, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val btnDrawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#3498DB"))
                    cornerRadius = 8f
                }
                background = btnDrawable
                setOnClickListener {
                    showEditScheduleDialog(consultantId, consultantName, dayOfWeek, days[i], timeText)
                }
            }

            dayLayout.addView(dayText)
            dayLayout.addView(timeText)
            dayLayout.addView(editButton)
            card.addView(dayLayout)
        }

        return card
    }

    /** Диалог редактирования расписания для конкретного дня */
    private fun showEditScheduleDialog(consultantId: Int, consultantName: String, dayOfWeek: Int, dayName: String, timeText: TextView) {
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
            text = "$consultantName - $dayName"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        dialogLayout.addView(title)

        val startButton = Button(this).apply {
            text = "Время начала"
            setBackgroundColor(Color.parseColor("#3498DB"))
            setTextColor(Color.WHITE)
            setPadding(20, 15, 20, 15)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 15) }
        }
        dialogLayout.addView(startButton)

        val endButton = Button(this).apply {
            text = "Время окончания"
            setBackgroundColor(Color.parseColor("#3498DB"))
            setTextColor(Color.WHITE)
            setPadding(20, 15, 20, 15)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 15) }
        }
        dialogLayout.addView(endButton)

        val dayOffButton = Button(this).apply {
            text = "Выходной"
            setBackgroundColor(Color.parseColor("#E74C3C"))
            setTextColor(Color.WHITE)
            setPadding(20, 15, 20, 15)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 15) }
        }
        dialogLayout.addView(dayOffButton)

        val saveButton = Button(this).apply {
            text = "Сохранить"
            setBackgroundColor(Color.parseColor("#27AE60"))
            setTextColor(Color.WHITE)
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val btnDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#27AE60"))
                cornerRadius = 12f
            }
            background = btnDrawable
        }
        dialogLayout.addView(saveButton)

        var selectedStart = ""
        var selectedEnd = ""

        val calendar = Calendar.getInstance()

        startButton.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedStart = String.format("%02d:%02d:00", hour, minute)
                startButton.text = String.format("%02d:%02d", hour, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        endButton.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedEnd = String.format("%02d:%02d:00", hour, minute)
                endButton.text = String.format("%02d:%02d", hour, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        dayOffButton.setOnClickListener {
            selectedStart = ""
            selectedEnd = ""
            startButton.text = "Время начала"
            endButton.text = "Время окончания"
            Toast.makeText(this, "Установлен выходной", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            if (selectedStart.isNotEmpty() && selectedEnd.isNotEmpty()) {
                saveSchedule(consultantId, dayOfWeek, selectedStart, selectedEnd, timeText)
            } else if (selectedStart.isEmpty() && selectedEnd.isEmpty()) {
                saveSchedule(consultantId, dayOfWeek, "", "", timeText)
            } else {
                Toast.makeText(this, "Выберите оба времени", Toast.LENGTH_SHORT).show()
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setCancelable(true)
            .create()
        dialog.show()
    }

    /** Сохранение расписания на сервере */
    private fun saveSchedule(consultantId: Int, dayOfWeek: Int, startTime: String, endTime: String, timeText: TextView) {
        showLoading(true)
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/schedule")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val isWorking = if (startTime.isNotEmpty() && endTime.isNotEmpty()) 1 else 0
                val jsonBody = JSONObject().apply {
                    put("ConsultantID", consultantId)
                    put("WeekDay", dayOfWeek)
                    put("StartTime", startTime)
                    put("EndTime", endTime)
                    put("BreakStart", JSONObject.NULL)
                    put("BreakEnd", JSONObject.NULL)
                    put("IsWorking", isWorking)
                }

                conn.outputStream.write(jsonBody.toString().toByteArray())

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                runOnUiThread {
                    showLoading(false)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Сохранено!", Toast.LENGTH_SHORT).show()
                        if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                            timeText.text = "${startTime.substring(0, 5)} - ${endTime.substring(0, 5)}"
                        } else {
                            timeText.text = "Выходной"
                        }
                    } else {
                        Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Загрузка записей на сегодня с учетом выбранного консультанта */
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
                    if (selectedConsultantId == 0 || obj.getInt("ConsultantID") == selectedConsultantId) {
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

    /** Загрузка записей на неделю с учетом выбранного консультанта */
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
                    if (selectedConsultantId == 0 || obj.getInt("ConsultantID") == selectedConsultantId) {
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

    /** Создание карточки записи для администратора */
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

        val consultantText = TextView(this).apply {
            text = consultantName
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#2C3E50"))
            setPadding(0, 0, 0, 8)
        }
        card.addView(consultantText)

        val clientText = TextView(this).apply {
            text = "Клиент: $clientName"
            textSize = 14f
            setTextColor(Color.parseColor("#7F8C8D"))
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

        if (status == "В ожидании") {
            val buttonLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val confirmButton = Button(this).apply {
                text = "Подтвердить"
                setBackgroundColor(Color.parseColor("#27AE60"))
                setTextColor(Color.WHITE)
                setAllCaps(false)
                textSize = 12f
                setPadding(12, 10, 12, 10)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                val btnDrawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#27AE60"))
                    cornerRadius = 8f
                }
                background = btnDrawable
                setOnClickListener {
                    updateStatus(appointmentId, "Подтверждена")
                }
            }

            val rejectButton = Button(this).apply {
                text = "Отклонить"
                setBackgroundColor(Color.parseColor("#E74C3C"))
                setTextColor(Color.WHITE)
                setAllCaps(false)
                textSize = 12f
                setPadding(12, 10, 12, 10)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                val btnDrawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#E74C3C"))
                    cornerRadius = 8f
                }
                background = btnDrawable
                setOnClickListener {
                    updateStatus(appointmentId, "Отклонена")
                }
            }

            buttonLayout.addView(confirmButton)
            buttonLayout.addView(rejectButton)
            card.addView(buttonLayout)
        } else if (status == "Подтверждена") {
            val completeButton = Button(this).apply {
                text = "Завершить"
                setBackgroundColor(Color.parseColor("#3498DB"))
                setTextColor(Color.WHITE)
                setAllCaps(false)
                textSize = 12f
                setPadding(12, 10, 12, 10)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val btnDrawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#3498DB"))
                    cornerRadius = 8f
                }
                background = btnDrawable
                setOnClickListener {
                    updateStatus(appointmentId, "Выполнена")
                }
            }
            card.addView(completeButton)
        }

        return card
    }

    /** Обновление статуса записи */
    private fun updateStatus(appointmentId: Int, newStatus: String) {
        showLoading(true)
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/update-status")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonBody = JSONObject().apply {
                    put("AppointmentID", appointmentId)
                    put("Status", newStatus)
                }

                conn.outputStream.write(jsonBody.toString().toByteArray())

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                runOnUiThread {
                    showLoading(false)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Статус обновлен: $newStatus", Toast.LENGTH_SHORT).show()
                        if (currentView == "today") loadTodayAppointments() else loadWeekAppointments()
                    } else {
                        Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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