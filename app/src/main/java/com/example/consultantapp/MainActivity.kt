package com.example.consultantapp

import android.app.DatePickerDialog
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

/*** Главный экран для роли "Клиент"*/
class MainActivity : AppCompatActivity() {

    private var userId: Int = 0
    private var userRole: String = ""
    private lateinit var mainLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout
    private lateinit var loadingPanel: LinearLayout
    private lateinit var tabServices: TextView
    private lateinit var tabConsultants: TextView
    private lateinit var tabAppointments: TextView
    private lateinit var consultantsContainer: LinearLayout
    private lateinit var servicesContainer: LinearLayout
    private val consultantsList = mutableListOf<Pair<Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = intent.getIntExtra("USER_ID", 1)
        userRole = intent.getStringExtra("USER_ROLE") ?: "Клиент"

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

        val topRow = androidx.constraintlayout.widget.ConstraintLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val params = layoutParams as LinearLayout.LayoutParams
            params.topMargin = 30
            layoutParams = params
        }

        val notificationButton = ImageView(this).apply {
            setImageResource(R.drawable.bell)
            setColorFilter(Color.parseColor("#2C3E50"))
            id = View.generateViewId()
            setOnClickListener {
                val dialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("Уведомления")
                    .setMessage("Напоминание: У вас запись завтра в 10:00")
                    .setPositiveButton("OK", null)
                    .create()
                dialog.show()
            }
        }
        topRow.addView(notificationButton)

        val title = TextView(this).apply {
            text = "Консультант"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#2C3E50"))
            id = View.generateViewId()
        }
        topRow.addView(title)

        val logoutButton = TextView(this).apply {
            text = "Выйти"
            textSize = 14f
            setTextColor(Color.parseColor("#E74C3C"))
            setPadding(20, 8, 0, 8)
            id = View.generateViewId()
            setOnClickListener {
                getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()
            }
        }
        topRow.addView(logoutButton)

        val set = androidx.constraintlayout.widget.ConstraintSet()
        set.constrainWidth(notificationButton.id, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        set.constrainHeight(notificationButton.id, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        set.connect(notificationButton.id, androidx.constraintlayout.widget.ConstraintSet.START, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.START)
        set.connect(notificationButton.id, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP)
        set.connect(notificationButton.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)

        set.constrainWidth(title.id, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        set.constrainHeight(title.id, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        set.connect(title.id, androidx.constraintlayout.widget.ConstraintSet.START, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.START)
        set.connect(title.id, androidx.constraintlayout.widget.ConstraintSet.END, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.END)
        set.connect(title.id, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP)
        set.connect(title.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)

        set.constrainWidth(logoutButton.id, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        set.constrainHeight(logoutButton.id, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        set.connect(logoutButton.id, androidx.constraintlayout.widget.ConstraintSet.END, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.END)
        set.connect(logoutButton.id, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP)
        set.connect(logoutButton.id, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM)

        set.applyTo(topRow)

        header.addView(topRow)

        val tabLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 20, 40, 0)
            setBackgroundColor(Color.WHITE)
        }

        tabServices = TextView(this).apply {
            text = "Услуги"
            textSize = 16f
            setTextColor(Color.parseColor("#3498DB"))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 15, 0, 15)
            setOnClickListener { switchTab(0) }
        }

        tabConsultants = TextView(this).apply {
            text = "Консультанты"
            textSize = 16f
            setTextColor(Color.parseColor("#7F8C8D"))
            setTypeface(null, Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 15, 0, 15)
            setOnClickListener { switchTab(1) }
        }

        tabAppointments = TextView(this).apply {
            text = "Мои записи"
            textSize = 16f
            setTextColor(Color.parseColor("#7F8C8D"))
            setTypeface(null, Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 15, 0, 15)
            setOnClickListener { switchTab(2) }
        }

        val tabUnderline = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
            )
            setBackgroundColor(Color.parseColor("#3498DB"))
        }

        tabLayout.addView(tabServices)
        tabLayout.addView(tabConsultants)
        tabLayout.addView(tabAppointments)

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

        val scrollServices = ScrollView(this)
        servicesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 20, 30, 100)
            visibility = View.VISIBLE
        }
        scrollServices.addView(servicesContainer)

        val scrollConsultants = ScrollView(this)
        consultantsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 20, 30, 100)
            visibility = View.GONE
        }
        scrollConsultants.addView(consultantsContainer)

        val scrollAppointments = ScrollView(this)
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 20, 30, 100)
            visibility = View.GONE
        }
        scrollAppointments.addView(contentLayout)

        mainLayout.addView(header)
        mainLayout.addView(tabLayout)
        mainLayout.addView(tabUnderline)
        mainLayout.addView(loadingPanel)
        mainLayout.addView(scrollServices)
        mainLayout.addView(scrollConsultants)
        mainLayout.addView(scrollAppointments)
        setContentView(mainLayout)

        loadConsultants()
        loadServices()
        loadMyAppointments()
    }

    /*** Переключение между вкладками*/
    private fun switchTab(tab: Int) {
        when (tab) {
            0 -> {
                servicesContainer.visibility = View.VISIBLE
                consultantsContainer.visibility = View.GONE
                contentLayout.visibility = View.GONE
                tabServices.setTextColor(Color.parseColor("#3498DB"))
                tabServices.setTypeface(null, Typeface.BOLD)
                tabConsultants.setTextColor(Color.parseColor("#7F8C8D"))
                tabConsultants.setTypeface(null, Typeface.NORMAL)
                tabAppointments.setTextColor(Color.parseColor("#7F8C8D"))
                tabAppointments.setTypeface(null, Typeface.NORMAL)
            }
            1 -> {
                servicesContainer.visibility = View.GONE
                consultantsContainer.visibility = View.VISIBLE
                contentLayout.visibility = View.GONE
                tabConsultants.setTextColor(Color.parseColor("#3498DB"))
                tabConsultants.setTypeface(null, Typeface.BOLD)
                tabServices.setTextColor(Color.parseColor("#7F8C8D"))
                tabServices.setTypeface(null, Typeface.NORMAL)
                tabAppointments.setTextColor(Color.parseColor("#7F8C8D"))
                tabAppointments.setTypeface(null, Typeface.NORMAL)
            }
            2 -> {
                servicesContainer.visibility = View.GONE
                consultantsContainer.visibility = View.GONE
                contentLayout.visibility = View.VISIBLE
                tabAppointments.setTextColor(Color.parseColor("#3498DB"))
                tabAppointments.setTypeface(null, Typeface.BOLD)
                tabServices.setTextColor(Color.parseColor("#7F8C8D"))
                tabServices.setTypeface(null, Typeface.NORMAL)
                tabConsultants.setTextColor(Color.parseColor("#7F8C8D"))
                tabConsultants.setTypeface(null, Typeface.NORMAL)
                loadMyAppointments()
            }
        }
    }

    /*** Загрузка списка услуг из API*/
    private fun loadServices() {
        showLoading(true)
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/services")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONArray(response)

                runOnUiThread {
                    showLoading(false)
                    servicesContainer.removeAllViews()
                    for (i in 0 until json.length()) {
                        val obj = json.getJSONObject(i)
                        val serviceId = obj.getInt("ServiceID")
                        val name = obj.getString("ServiceName")
                        val duration = obj.getInt("DurationMinutes")
                        val price = obj.getDouble("Price")
                        val desc = obj.optString("Description", "Профессиональная консультация")

                        servicesContainer.addView(createServiceCard(serviceId, name, desc, duration, price))
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showLoading(false)
                    showError(servicesContainer, e.message)
                }
            }
        }
    }

    /*** Загрузка списка консультантов из API*/
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
                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)
                    consultantsList.add(Pair(obj.getInt("UserID"), obj.getString("FullName")))
                }

                runOnUiThread {
                    consultantsContainer.removeAllViews()
                    if (json.length() == 0) {
                        val emptyText = TextView(this@MainActivity).apply {
                            text = "Нет консультантов"
                            textSize = 14f
                            setTextColor(Color.parseColor("#7F8C8D"))
                            gravity = android.view.Gravity.CENTER
                            setPadding(0, 50, 0, 0)
                        }
                        consultantsContainer.addView(emptyText)
                    } else {
                        for (i in 0 until json.length()) {
                            val obj = json.getJSONObject(i)
                            val name = obj.getString("FullName")
                            val spec = obj.optString("Specialization", "Специалист")
                            val consultantId = obj.getInt("UserID")
                            val photoUrl = obj.optString("PhotoURL", "")
                            consultantsContainer.addView(createConsultantCard(name, spec, photoUrl, consultantId))
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val errorText = TextView(this@MainActivity).apply {
                        text = "Ошибка загрузки: ${e.message}"
                        textSize = 14f
                        setTextColor(Color.RED)
                        setPadding(30, 50, 30, 50)
                    }
                    consultantsContainer.addView(errorText)
                }
            }
        }
    }

    /*** Загрузка списка записей текущего пользователя*/
    private fun loadMyAppointments() {
        contentLayout.removeAllViews()
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/my-appointments/$userId")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONArray(response)

                runOnUiThread {
                    if (json.length() == 0) {
                        val emptyText = TextView(this@MainActivity).apply {
                            text = "У вас пока нет записей.\nНажмите \"Записаться\" на любую услугу"
                            textSize = 14f
                            setTextColor(Color.parseColor("#7F8C8D"))
                            gravity = android.view.Gravity.CENTER
                            setPadding(0, 50, 0, 0)
                        }
                        contentLayout.addView(emptyText)
                    } else {
                        for (i in 0 until json.length()) {
                            val obj = json.getJSONObject(i)
                            val consultantName = obj.getString("ConsultantName")
                            val serviceName = obj.getString("ServiceName")
                            val date = obj.getString("AppointmentDate")
                            val startTime = obj.getString("StartTime")
                            val status = obj.getString("Status")
                            val appointmentId = obj.getInt("AppointmentID")

                            contentLayout.addView(createAppointmentCard(consultantName, serviceName, date, startTime, status, appointmentId))
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError(contentLayout, e.message)
                }
            }
        }
    }

    /*** Создание карточки услуги*/
    private fun createServiceCard(serviceId: Int, name: String, desc: String, duration: Int, price: Double): LinearLayout {
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
            cornerRadius = 24f
            setStroke(1, Color.parseColor("#E0E0E0"))
        }
        card.background = drawable

        val nameText = TextView(this).apply {
            text = name
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#2C3E50"))
            setPadding(0, 0, 0, 8)
        }

        val descText = TextView(this).apply {
            text = desc
            textSize = 13f
            setTextColor(Color.parseColor("#7F8C8D"))
            setPadding(0, 0, 0, 15)
        }

        val infoRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val durationText = TextView(this).apply {
            text = "⏱ $duration мин"
            textSize = 13f
            setTextColor(Color.parseColor("#3498DB"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val priceText = TextView(this).apply {
            text = "$price ₽"
            textSize = 13f
            setTextColor(Color.parseColor("#27AE60"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.END
        }

        infoRow.addView(durationText)
        infoRow.addView(priceText)

        val button = Button(this).apply {
            text = "Записаться"
            setBackgroundColor(Color.parseColor("#3498DB"))
            setTextColor(Color.WHITE)
            setAllCaps(false)
            setPadding(20, 12, 20, 12)
            setOnClickListener {
                showBookingDialog(serviceId, name)
            }
        }

        card.addView(nameText)
        card.addView(descText)
        card.addView(infoRow)
        card.addView(button)

        return card
    }

    /*** Создание карточки консультанта*/
    private fun createConsultantCard(name: String, spec: String, photoUrl: String, consultantId: Int): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 15) }
            setPadding(30, 25, 30, 25)
            setBackgroundColor(Color.WHITE)
        }

        val drawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 16f
            setStroke(1, Color.parseColor("#E0E0E0"))
        }
        card.background = drawable

        val photoImageView = android.widget.ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                setMargins(0, 0, 20, 0)
            }
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            setImageResource(android.R.drawable.ic_menu_gallery)
        }

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nameText = TextView(this).apply {
            text = name
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#2C3E50"))
            setPadding(0, 0, 0, 8)
        }

        val specText = TextView(this).apply {
            text = spec
            textSize = 13f
            setTextColor(Color.parseColor("#7F8C8D"))
            setPadding(0, 0, 0, 0)
        }

        textLayout.addView(nameText)
        textLayout.addView(specText)

        card.addView(photoImageView)
        card.addView(textLayout)

        return card
    }

    /*** Создание карточки записи (для истории записей)*/
    private fun createAppointmentCard(consultantName: String, serviceName: String, date: String, startTime: String, status: String, appointmentId: Int): LinearLayout {
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

        val consultantText = TextView(this).apply {
            text = consultantName
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#2C3E50"))
            setPadding(0, 0, 0, 8)
        }

        val serviceText = TextView(this).apply {
            text = serviceName
            textSize = 14f
            setTextColor(Color.parseColor("#7F8C8D"))
            setPadding(0, 0, 0, 8)
        }

        val formattedDate = try {
            date.split("T")[0].split("-").reversed().joinToString(".")
        } catch (e: Exception) {
            date
        }

        val formattedTime = try {
            startTime.split("T")[1].split(".")[0].substring(0, 5)
        } catch (e: Exception) {
            startTime
        }

        val dateTimeText = TextView(this).apply {
            text = "$formattedDate в $formattedTime"
            textSize = 13f
            setTextColor(Color.parseColor("#3498DB"))
            setPadding(0, 0, 0, 8)
        }

        val statusText = TextView(this).apply {
            text = when (status) {
                "Подтверждена" -> "Подтверждена"
                "Отменена" -> "Отменена"
                "Выполнена" -> "Выполнена"
                else -> status
            }
            textSize = 13f
            val color = when (status) {
                "Подтверждена" -> Color.parseColor("#27AE60")
                "Отменена" -> Color.parseColor("#E74C3C")
                "Выполнена" -> Color.parseColor("#95A5A6")
                else -> Color.parseColor("#F39C12")
            }
            setTextColor(color)
            setPadding(0, 0, 0, 10)
        }

        card.addView(consultantText)
        card.addView(serviceText)
        card.addView(dateTimeText)
        card.addView(statusText)

        if (status == "Подтверждена" || status == "В ожидании") {
            val buttonLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 15, 0, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val cancelButton = Button(this).apply {
                text = "Отменить"
                setBackgroundColor(Color.parseColor("#464646"))
                setTextColor(Color.WHITE)
                setAllCaps(false)
                textSize = 12f
                setPadding(12, 8, 12, 8)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    cancelAppointment(appointmentId)
                }
            }

            val rescheduleButton = Button(this).apply {
                text = "Перенести"
                setBackgroundColor(Color.parseColor("#3498DB"))
                setTextColor(Color.WHITE)
                setAllCaps(false)
                textSize = 12f
                setPadding(12, 8, 12, 8)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    rescheduleAppointment(appointmentId, date, startTime)
                }
            }

            buttonLayout.addView(cancelButton)
            buttonLayout.addView(rescheduleButton)
            card.addView(buttonLayout)
        }

        return card
    }

    /*** Отображение диалога бронирования услуги*/
    private fun showBookingDialog(serviceId: Int, serviceName: String) {
        if (consultantsList.isEmpty()) {
            Toast.makeText(this, "Список консультантов еще не загружен.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.WHITE)
        }

        val dialogDrawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 24f
        }
        dialogLayout.background = dialogDrawable

        val title = TextView(this).apply {
            text = "Запись на: $serviceName"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }
        dialogLayout.addView(title)

        val consultantContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 25)
        }
        dialogLayout.minimumHeight = 600

        val consultantLabel = TextView(this).apply {
            text = "Выберите консультанта"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        }
        consultantContainer.addView(consultantLabel)

        val consultantSpinner = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, consultantsList.map { it.second })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        consultantSpinner.adapter = adapter
        consultantSpinner.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val spinnerParams = consultantSpinner.layoutParams as LinearLayout.LayoutParams
        spinnerParams.topMargin = 8
        consultantSpinner.layoutParams = spinnerParams
        consultantContainer.addView(consultantSpinner)
        dialogLayout.addView(consultantContainer)

        val dateContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 20)
        }

        val dateLabel = TextView(this).apply {
            text = "Выберите дату"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        }
        dateContainer.addView(dateLabel)

        val dateButton = Button(this).apply {
            text = "Выбрать дату"
            setBackgroundColor(Color.parseColor("#3498DB"))
            setTextColor(Color.WHITE)
            setPadding(20, 15, 20, 15)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val btnDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#3498DB"))
                cornerRadius = 12f
            }
            background = btnDrawable
        }
        dateContainer.addView(dateButton)
        dialogLayout.addView(dateContainer)

        val timeContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 20)
            visibility = View.GONE
        }

        val timeLabel = TextView(this).apply {
            text = "Выберите время"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        }
        timeContainer.addView(timeLabel)

        val timeSlotsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 0)
        }
        timeContainer.addView(timeSlotsContainer)
        dialogLayout.addView(timeContainer)

        val commentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 30)
        }

        val commentLabel = TextView(this).apply {
            text = "Комментарий"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        }
        commentContainer.addView(commentLabel)

        val commentInput = EditText(this).apply {
            hint = "Необязательно"
            setPadding(20, 15, 20, 15)
            background = createEditTextBackground()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        commentContainer.addView(commentInput)
        dialogLayout.addView(commentContainer)

        val submitContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 0)
        }

        val submitButton = Button(this).apply {
            text = "Подтвердить запись"
            setTextColor(Color.WHITE)
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val btnDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#3498DB"))
                cornerRadius = 12f
            }
            background = btnDrawable
        }
        submitContainer.addView(submitButton)
        dialogLayout.addView(submitContainer)

        var selectedConsultantId = 0
        var selectedDate = ""
        var selectedTime = ""

        consultantSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedConsultantId = consultantsList[position].first
                selectedDate = ""
                selectedTime = ""
                dateButton.text = "Выбрать дату"
                timeContainer.visibility = View.GONE
                timeSlotsContainer.removeAllViews()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val calendar = Calendar.getInstance()
        dateButton.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$year-${month + 1}-$day"
                dateButton.text = "$day.${month + 1}.$year"
                selectedTime = ""

                timeContainer.visibility = View.GONE
                timeSlotsContainer.removeAllViews()
                val loadingTimeSlots = ProgressBar(this@MainActivity)
                timeSlotsContainer.addView(loadingTimeSlots)
                timeContainer.visibility = View.VISIBLE

                thread {
                    try {
                        val url = URL("http://10.0.2.2:3000/api/booked-slots/$selectedConsultantId/$selectedDate")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.connectTimeout = 5000
                        val response = conn.inputStream.bufferedReader().readText()
                        val bookedSlotsJson = JSONArray(response)
                        val bookedSlots = mutableSetOf<String>()
                        for (i in 0 until bookedSlotsJson.length()) {
                            bookedSlots.add(bookedSlotsJson.getString(i))
                        }

                        val allSlots = mutableListOf<String>()
                        for (hour in 9..17) {
                            allSlots.add(String.format("%02d:00:00", hour))
                        }
                        val freeSlots = allSlots.filter { it !in bookedSlots }

                        runOnUiThread {
                            timeSlotsContainer.removeAllViews()
                            if (freeSlots.isEmpty()) {
                                val tv = TextView(this@MainActivity).apply {
                                    text = "На эту дату нет свободных слотов"
                                    textSize = 14f
                                    setTextColor(Color.RED)
                                    setPadding(10, 20, 10, 20)
                                }
                                timeSlotsContainer.addView(tv)
                            } else {
                                val gridLayout = LinearLayout(this@MainActivity).apply {
                                    orientation = LinearLayout.VERTICAL
                                }
                                var currentRow = LinearLayout(this@MainActivity).apply {
                                    orientation = LinearLayout.HORIZONTAL
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                }
                                var colCount = 0
                                for (slot in freeSlots) {
                                    val hourMin = slot.substring(0, 5)
                                    val isAvailable = true

                                    val slotButton = Button(this@MainActivity).apply {
                                        text = hourMin
                                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                                            setMargins(4, 4, 4, 4)
                                        }

                                        if (isAvailable) {
                                            setBackgroundColor(Color.WHITE)
                                            setTextColor(Color.parseColor("#3498DB"))
                                            val btnDrawable = GradientDrawable().apply {
                                                setColor(Color.WHITE)
                                                cornerRadius = 8f
                                                setStroke(1, Color.parseColor("#3498DB"))
                                            }
                                            background = btnDrawable
                                        } else {
                                            setBackgroundColor(Color.parseColor("#E0E0E0"))
                                            setTextColor(Color.parseColor("#9E9E9E"))
                                            isEnabled = false
                                        }

                                        setOnClickListener {
                                            if (isAvailable) {
                                                selectedTime = slot
                                                Toast.makeText(this@MainActivity, "Выбрано время: $hourMin", Toast.LENGTH_SHORT).show()
                                                for (i in 0 until gridLayout.childCount) {
                                                    val row = gridLayout.getChildAt(i) as LinearLayout
                                                    for (j in 0 until row.childCount) {
                                                        val btn = row.getChildAt(j) as Button
                                                        btn.setBackgroundColor(Color.WHITE)
                                                        btn.setTextColor(Color.parseColor("#3498DB"))
                                                        val btnDrawable = GradientDrawable().apply {
                                                            setColor(Color.WHITE)
                                                            cornerRadius = 8f
                                                            setStroke(1, Color.parseColor("#3498DB"))
                                                        }
                                                        btn.background = btnDrawable
                                                    }
                                                }
                                                setBackgroundColor(Color.parseColor("#3498DB"))
                                                setTextColor(Color.WHITE)
                                                val selectedDrawable = GradientDrawable().apply {
                                                    setColor(Color.parseColor("#3498DB"))
                                                    cornerRadius = 8f
                                                }
                                                background = selectedDrawable
                                            }
                                        }
                                    }
                                    currentRow.addView(slotButton)
                                    colCount++
                                    if (colCount == 3) {
                                        gridLayout.addView(currentRow)
                                        currentRow = LinearLayout(this@MainActivity).apply {
                                            orientation = LinearLayout.HORIZONTAL
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                        }
                                        colCount = 0
                                    }
                                }
                                if (colCount > 0) {
                                    while (colCount < 3) {
                                        val emptyView = View(this@MainActivity).apply {
                                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                            visibility = View.INVISIBLE
                                        }
                                        currentRow.addView(emptyView)
                                        colCount++
                                    }
                                    gridLayout.addView(currentRow)
                                }
                                timeSlotsContainer.addView(gridLayout)
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            timeSlotsContainer.removeAllViews()
                            val tv = TextView(this@MainActivity).apply {
                                text = "Ошибка загрузки слотов: ${e.message}"
                                textSize = 14f
                                setTextColor(Color.RED)
                                setPadding(10, 20, 10, 20)
                            }
                            timeSlotsContainer.addView(tv)
                        }
                    }
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        submitButton.setOnClickListener {
            if (selectedConsultantId != 0 && selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                createAppointment(serviceId, selectedConsultantId, selectedDate, selectedTime, commentInput.text.toString())
            } else {
                Toast.makeText(this, "Выберите консультанта, дату и время", Toast.LENGTH_SHORT).show()
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setCancelable(true)
            .create()
        dialog.show()
    }

    /*** Отправка запроса на создание записи*/
    private fun createAppointment(serviceId: Int, consultantId: Int, date: String, time: String, comment: String) {
        showLoading(true)
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/appointments")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonBody = JSONObject().apply {
                    put("ClientID", userId)
                    put("ConsultantID", consultantId)
                    put("ServiceID", serviceId)
                    put("AppointmentDate", date)
                    put("StartTime", time)
                    put("EndTime", "12:00:00")
                    put("Comment", comment)
                }

                conn.outputStream.write(jsonBody.toString().toByteArray())

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                runOnUiThread {
                    showLoading(false)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Запись создана!", Toast.LENGTH_SHORT).show()
                        loadMyAppointments()
                        switchTab(2)
                    } else {
                        Toast.makeText(this, "Ошибка при создании записи", Toast.LENGTH_SHORT).show()
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

    /*** Отмена записи*/
    private fun cancelAppointment(appointmentId: Int) {
        showLoading(true)
        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/cancel-appointment/$appointmentId")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PUT"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.write("{}".toByteArray())

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                runOnUiThread {
                    showLoading(false)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Запись отменена", Toast.LENGTH_SHORT).show()
                        loadMyAppointments()
                    } else {
                        Toast.makeText(this, "Ошибка при отмене", Toast.LENGTH_SHORT).show()
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

    /*** Отображение/скрытие панели загрузки*/
    private fun showLoading(show: Boolean) {
        loadingPanel.visibility = if (show) View.VISIBLE else View.GONE
    }

    /*** Отображение сообщения об ошибке*/
    private fun showError(container: LinearLayout, message: String?) {
        val errorText = TextView(this).apply {
            text = "Ошибка: ${message ?: "неизвестная ошибка"}"
            textSize = 14f
            setTextColor(Color.RED)
            setPadding(30, 50, 30, 50)
        }
        container.addView(errorText)
    }

    /*** Перенос записи на новую дату и время*/
    private fun rescheduleAppointment(appointmentId: Int, currentDate: String, currentTime: String) {
        if (consultantsList.isEmpty()) {
            Toast.makeText(this, "Список консультантов не загружен", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.WHITE)
        }

        val dialogDrawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 24f
        }
        dialogLayout.background = dialogDrawable

        val title = TextView(this).apply {
            text = "Перенос записи"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }
        dialogLayout.addView(title)

        var selectedConsultantId = 0
        var selectedDate = ""
        var selectedTime = ""
        var currentConsultantId = 0

        val infoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 25)
            visibility = View.GONE
        }

        thread {
            try {
                val url = URL("http://10.0.2.2:3000/api/my-appointments/$userId")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONArray(response)
                for (i in 0 until json.length()) {
                    val obj = json.getJSONObject(i)
                    if (obj.getInt("AppointmentID") == appointmentId) {
                        currentConsultantId = obj.getInt("ConsultantID")
                        break
                    }
                }
                runOnUiThread {
                    selectedConsultantId = currentConsultantId
                    val consultantName = consultantsList.find { it.first == currentConsultantId }?.second ?: "Консультант"

                    val infoText = TextView(this@MainActivity).apply {
                        text = "Консультант: $consultantName\nДата: ${currentDate.split("T")[0].split("-").reversed().joinToString(".")}\nВремя: ${currentTime.split("T")[1].split(".")[0].substring(0, 5)}"
                        textSize = 16f
                        setTextColor(Color.BLACK)
                        setPadding(0, 0, 0, 10)
                    }
                    infoContainer.addView(infoText)
                    infoContainer.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dialogLayout.addView(infoContainer)

        val dateContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 20)
        }

        val dateLabel = TextView(this).apply {
            text = "Выберите новую дату"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        }
        dateContainer.addView(dateLabel)

        val dateButton = Button(this).apply {
            text = "Выбрать дату"
            setBackgroundColor(Color.parseColor("#3498DB"))
            setTextColor(Color.WHITE)
            setPadding(20, 15, 20, 15)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val btnDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#3498DB"))
                cornerRadius = 12f
            }
            background = btnDrawable
        }
        dateContainer.addView(dateButton)
        dialogLayout.addView(dateContainer)

        val timeContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 20)
            visibility = View.GONE
        }

        val timeLabel = TextView(this).apply {
            text = "Выберите новое время"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        }
        timeContainer.addView(timeLabel)

        val timeSlotsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 0)
        }
        timeContainer.addView(timeSlotsContainer)
        dialogLayout.addView(timeContainer)

        val submitContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 0)
        }

        val submitButton = Button(this).apply {
            text = "Подтвердить перенос"
            setTextColor(Color.WHITE)
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val btnDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#3498DB"))
                cornerRadius = 12f
            }
            background = btnDrawable
        }
        submitContainer.addView(submitButton)
        dialogLayout.addView(submitContainer)

        dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$year-${month + 1}-$day"
                dateButton.text = "$day.${month + 1}.$year"
                selectedTime = ""

                timeContainer.visibility = View.GONE
                timeSlotsContainer.removeAllViews()
                val loadingTimeSlots = ProgressBar(this@MainActivity)
                timeSlotsContainer.addView(loadingTimeSlots)
                timeContainer.visibility = View.VISIBLE

                thread {
                    try {
                        val url = URL("http://10.0.2.2:3000/api/booked-slots/$selectedConsultantId/$selectedDate")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.connectTimeout = 5000
                        val response = conn.inputStream.bufferedReader().readText()
                        val bookedSlotsJson = JSONArray(response)
                        val bookedSlots = mutableSetOf<String>()
                        for (i in 0 until bookedSlotsJson.length()) {
                            bookedSlots.add(bookedSlotsJson.getString(i))
                        }

                        val allSlots = mutableListOf<String>()
                        for (hour in 9..17) {
                            allSlots.add(String.format("%02d:00:00", hour))
                        }
                        val freeSlots = allSlots.filter { it !in bookedSlots }

                        runOnUiThread {
                            timeSlotsContainer.removeAllViews()
                            if (freeSlots.isEmpty()) {
                                val tv = TextView(this@MainActivity).apply {
                                    text = "На эту дату нет свободных слотов"
                                    textSize = 14f
                                    setTextColor(Color.RED)
                                    setPadding(10, 20, 10, 20)
                                }
                                timeSlotsContainer.addView(tv)
                            } else {
                                val gridLayout = LinearLayout(this@MainActivity).apply {
                                    orientation = LinearLayout.VERTICAL
                                }
                                var currentRow = LinearLayout(this@MainActivity).apply {
                                    orientation = LinearLayout.HORIZONTAL
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                }
                                var colCount = 0
                                for (slot in freeSlots) {
                                    val hourMin = slot.substring(0, 5)
                                    val isAvailable = true
                                    val slotButton = Button(this@MainActivity).apply {
                                        text = hourMin
                                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                                            setMargins(4, 4, 4, 4)
                                        }
                                        if (isAvailable) {
                                            setBackgroundColor(Color.WHITE)
                                            setTextColor(Color.parseColor("#3498DB"))
                                            val btnDrawable = GradientDrawable().apply {
                                                setColor(Color.WHITE)
                                                cornerRadius = 8f
                                                setStroke(1, Color.parseColor("#3498DB"))
                                            }
                                            background = btnDrawable
                                        } else {
                                            setBackgroundColor(Color.parseColor("#E0E0E0"))
                                            setTextColor(Color.parseColor("#9E9E9E"))
                                            isEnabled = false
                                        }
                                        setOnClickListener {
                                            if (isAvailable) {
                                                selectedTime = slot
                                                Toast.makeText(this@MainActivity, "Выбрано время: $hourMin", Toast.LENGTH_SHORT).show()
                                                for (i in 0 until gridLayout.childCount) {
                                                    val row = gridLayout.getChildAt(i) as LinearLayout
                                                    for (j in 0 until row.childCount) {
                                                        val btn = row.getChildAt(j) as Button
                                                        btn.setBackgroundColor(Color.WHITE)
                                                        btn.setTextColor(Color.parseColor("#3498DB"))
                                                        val btnDrawable = GradientDrawable().apply {
                                                            setColor(Color.WHITE)
                                                            cornerRadius = 8f
                                                            setStroke(1, Color.parseColor("#3498DB"))
                                                        }
                                                        btn.background = btnDrawable
                                                    }
                                                }
                                                setBackgroundColor(Color.parseColor("#3498DB"))
                                                setTextColor(Color.WHITE)
                                                val selectedDrawable = GradientDrawable().apply {
                                                    setColor(Color.parseColor("#3498DB"))
                                                    cornerRadius = 8f
                                                }
                                                background = selectedDrawable
                                            }
                                        }
                                    }
                                    currentRow.addView(slotButton)
                                    colCount++
                                    if (colCount == 3) {
                                        gridLayout.addView(currentRow)
                                        currentRow = LinearLayout(this@MainActivity).apply {
                                            orientation = LinearLayout.HORIZONTAL
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                        }
                                        colCount = 0
                                    }
                                }
                                if (colCount > 0) {
                                    while (colCount < 3) {
                                        val emptyView = View(this@MainActivity).apply {
                                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                            visibility = View.INVISIBLE
                                        }
                                        currentRow.addView(emptyView)
                                        colCount++
                                    }
                                    gridLayout.addView(currentRow)
                                }
                                timeSlotsContainer.addView(gridLayout)
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            timeSlotsContainer.removeAllViews()
                            val tv = TextView(this@MainActivity).apply {
                                text = "Ошибка загрузки слотов: ${e.message}"
                                textSize = 14f
                                setTextColor(Color.RED)
                                setPadding(10, 20, 10, 20)
                            }
                            timeSlotsContainer.addView(tv)
                        }
                    }
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        submitButton.setOnClickListener {
            if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                showLoading(true)
                thread {
                    try {
                        val url = URL("http://10.0.2.2:3000/api/reschedule-appointment/$appointmentId")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "PUT"
                        conn.setRequestProperty("Content-Type", "application/json")
                        conn.doOutput = true

                        val jsonBody = JSONObject().apply {
                            put("NewDate", selectedDate)
                            put("NewTime", selectedTime)
                        }
                        conn.outputStream.write(jsonBody.toString().toByteArray())

                        val response = conn.inputStream.bufferedReader().readText()
                        val json = JSONObject(response)

                        runOnUiThread {
                            showLoading(false)
                            if (json.getBoolean("success")) {
                                Toast.makeText(this, "Запись перенесена!", Toast.LENGTH_SHORT).show()
                                loadMyAppointments()
                                (dialogLayout.parent as? androidx.appcompat.app.AlertDialog)?.dismiss()
                            } else {
                                Toast.makeText(this, "Ошибка переноса", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            showLoading(false)
                            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Выберите новую дату и время", Toast.LENGTH_SHORT).show()
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setCancelable(true)
            .create()
        dialog.show()
    }

    /*** Создание фона для полей ввод */
    private fun createEditTextBackground(): android.graphics.drawable.Drawable {
        val drawable = GradientDrawable()
        drawable.setColor(Color.parseColor("#F2F2F2"))
        drawable.cornerRadius = 12f
        drawable.setStroke(1, Color.parseColor("#E0E0E0"))
        return drawable
    }
}