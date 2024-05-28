    package com.example.simon;
    import android.content.ContentValues
    import android.database.sqlite.SQLiteDatabase
    import android.graphics.Color
    import android.graphics.Paint
    import android.graphics.PorterDuff
    import android.os.Bundle
    import android.os.Handler
    import android.os.Looper
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.MotionEvent
    import android.view.SurfaceHolder
    import android.view.View
    import android.widget.Button
    import android.widget.TextView
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import com.example.simon.databinding.ActivityMainBinding
    import com.example.simon.databinding.SaveScoreBinding
    import kotlin.math.log
    import kotlin.random.Random
    import android.view.animation.AnimationUtils
    data class Player(val name: String, val score: Int)


    class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
        private lateinit var binding: ActivityMainBinding
        private lateinit var soundManager: SoundManager
        private lateinit var dbHelper: PlayerDbHelper

        private val glow = 255
        private val dark = 128
        private var red = Color.argb(dark, 255, 0, 0)
        private var yellow = Color.argb(dark, 255, 255, 0)
        private var green = Color.argb(dark, 0, 255, 0)
        private var blue = Color.argb(dark,0, 0, 255)

        private val gameRandomList = mutableListOf<Int>()
        private var gameSequence = 1;
        private var playerSequence = 0;
        private var isClickEnabled = true
        private val clickDelayMillis: Long = 1000
        private var gameStart = false
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            soundManager = SoundManager(this)
            dbHelper = PlayerDbHelper(this)

            val surfaceView = binding.surfaceView
            surfaceView.holder.addCallback(this)
            binding.Title.text = "Play"
            surfaceView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val x = event.x
                        val y = event.y
                        detectTouch(x, y)
                        true
                    }
                    else -> false
                }
            }

            binding.boardIcon.setOnClickListener {
                showScoreBoardDialog()
            }

        }
        override fun surfaceCreated(holder: SurfaceHolder) {
            drawCanvas(holder)
        }
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        override fun surfaceDestroyed(holder: SurfaceHolder) {}
        private fun drawCanvas(holder: SurfaceHolder){
            val canvas = holder.lockCanvas()
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            val centerX = canvas.width / 2f
            val centerY = canvas.height / 2f
            val radius = (canvas.height.coerceAtMost(canvas.width) * 0.4).toFloat()
            val smallCircleRadius = 200f
            val paint = Paint().apply {
                isAntiAlias = true
            }
            //region draw colors
            paint.color = red
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                10f,
                70f,
                true,
                paint
            )
            paint.color = green
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                100f,
                70f,
                true,
                paint
            )
            paint.color = yellow
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                190f,
                70f,
                true,
                paint
            )
            paint.color = blue
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                280f,
                70f,
                true,
                paint
            )
            paint.color = Color.BLACK
            canvas.drawCircle(centerX, centerY, smallCircleRadius, paint)
            // endregion
            holder.unlockCanvasAndPost(canvas)
        }
        private fun changeColor(alpha: Int, selected: Int){
            when (selected){
                0 -> {
                    red = Color.argb(alpha, 255, 0, 0)
                }
                1 -> {
                    green = Color.argb(alpha, 0, 255, 0)
                }
                2 -> {
                    yellow = Color.argb(alpha, 255, 255, 0)
                }
                3 -> {
                    blue = Color.argb(alpha, 0, 0, 255)
                }
                else -> {
                    red = Color.argb(alpha, 255, 0, 0)
                    green = Color.argb(alpha, 0, 255, 0)
                    yellow = Color.argb(alpha, 255, 255, 0)
                    blue = Color.argb(alpha, 0, 0, 255)
                }
            }
        }
        private fun detectTouch(x: Float, y: Float) {

            var selected = -1;

            val centerX = binding.surfaceView.width / 2f
            val centerY = binding.surfaceView.height / 2f
            val radius = (binding.surfaceView.height.coerceAtMost(binding.surfaceView.width) * 0.4).toFloat()
            val smallCircleRadius = 200f

            val distance = Math.sqrt(Math.pow((x - centerX).toDouble(), 2.0) + Math.pow((y - centerY).toDouble(), 2.0)).toFloat()

            if (distance >= radius) {
                return
            }
            if (distance <= smallCircleRadius) {
                gameStart = !gameStart
                if(gameStart){
                    binding.Title.text = "Pause"
                    startGame()
                }else{
                    binding.Title.text = "Play"
                    binding.Title.clearAnimation()
                    saveSettings();
                }

                return
            }
            if(!gameStart){
                return
            }
            if (!isClickEnabled) {
                return
            }
            isClickEnabled = false

            val angle = Math.toDegrees(Math.atan2((y - centerY).toDouble(), (x - centerX).toDouble()))

            val adjustedAngle = if (angle < 0) angle + 360 else angle

             when {
                /*Red*/adjustedAngle >= 10 && adjustedAngle < 80 -> {
                    selected = 0
                    changeColor(glow,selected)
                }
                /*Green*/adjustedAngle >= 100 && adjustedAngle < 170 ->{
                    selected = 1
                    changeColor(glow,selected)
                }
                /*Yellow*/adjustedAngle >= 190 && adjustedAngle < 260 ->{
                    selected = 2
                    changeColor(glow,selected)
                }
                /*Blue*/adjustedAngle >= 280 && adjustedAngle < 350 -> {
                    selected = 3
                    changeColor(glow,selected)
                }
                else -> drawCanvas(binding.surfaceView.holder)
            }
            soundManager.playSound(selected)
            drawCanvas(binding.surfaceView.holder)
            Handler(Looper.getMainLooper()).postDelayed({
                changeColor(dark,selected)
                drawCanvas(binding.surfaceView.holder)
            }, clickDelayMillis)
            compareClick(selected);
        }
        private fun createGameArray(){
            gameRandomList.clear()
            repeat(50) {
                gameRandomList.add(Random.nextInt(4))
            }
        }
        private fun startGame() {
            if (!gameStart) {
                return
            }
            binding.Title.startAnimation(AnimationUtils.loadAnimation(this, R.anim.simon_rotate))
            createGameArray()
            gameSequence = 1
            playerSequence = 0
            playArray()
        }
        private fun loseGame() {
            gameStart = false
            binding.Title.clearAnimation()

            binding.Score.text = "Lose"
            binding.Title.text = "Play"
            saveSettings()
            isClickEnabled = true
        }
        private fun saveSettings() {
            val builder = AlertDialog.Builder(this)
            val dialogBinding = SaveScoreBinding.inflate(layoutInflater)
            val dialog = builder.create()
            dialogBinding.scoreTextView.text = "Score: $playerSequence"

            dialogBinding.closeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.saveButton.setOnClickListener {
                val name = dialogBinding.nameEditText.text.toString()
                saveScore(name, playerSequence)
                dialog.dismiss()
            }

            dialog.setView(dialogBinding.root)
            dialog.show()
        }

        private fun saveScore(name: String, score: Int) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(PlayerContract.PlayerEntry.COLUMN_NAME, name)
                put(PlayerContract.PlayerEntry.COLUMN_SCORE, score)
            }
            val newRowId = db.insertWithOnConflict(
                PlayerContract.PlayerEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }

        private fun playArray() {
            isClickEnabled = false
            val handler = Handler(Looper.getMainLooper())

            var currentIndex = 0

            fun changeColorDelayed(index: Int) {
                if (!gameStart) {
                    isClickEnabled = true
                    return
                }
                handler.postDelayed({
                    changeColor(glow, gameRandomList[index])
                    soundManager.playSound(gameRandomList[index])
                    drawCanvas(binding.surfaceView.holder)

                    handler.postDelayed({
                        changeColor(dark, gameRandomList[index])
                        drawCanvas(binding.surfaceView.holder)

                        if (index + 1 < gameSequence) {
                            changeColorDelayed(index + 1)
                        } else {
                            gameSequence++
                            isClickEnabled = true                        }
                    }, 1000)
                }, (1000).toLong())
            }
            changeColorDelayed(currentIndex)
        }
        private fun compareClick(selected: Int) {
            if (selected != gameRandomList[playerSequence]) {
                loseGame()
                return
            }

            playerSequence++
            if (playerSequence === gameSequence - 1) {
                binding.Score.text = playerSequence.toString()
                playerSequence = 0
                Handler(Looper.getMainLooper()).postDelayed({
                    playArray()
                }, clickDelayMillis)
            } else {
                isClickEnabled = true
            }
        }

        private fun showScoreBoardDialog() {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.board_score, null)
            val textViewScores = dialogView.findViewById<TextView>(R.id.textViewScores)
            val scores = getTopScores()

            val displayText = scores.joinToString(separator = "\n") { "Name: ${it.name}, Score: ${it.score}" }
            textViewScores.text = displayText

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialogView.findViewById<Button>(R.id.buttonClose).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
        private fun getTopScores(): List<Player> {
            val db = dbHelper.readableDatabase

            val query = "SELECT * FROM ${PlayerContract.PlayerEntry.TABLE_NAME} ORDER BY ${PlayerContract.PlayerEntry.COLUMN_SCORE} DESC LIMIT 5"
            val cursor = db.rawQuery(query, null)

            val players = mutableListOf<Player>()

            with(cursor) {
                while (moveToNext()) {
                    val name = getString(getColumnIndexOrThrow(PlayerContract.PlayerEntry.COLUMN_NAME))
                    val score = getInt(getColumnIndexOrThrow(PlayerContract.PlayerEntry.COLUMN_SCORE))
                    players.add(Player(name, score))
                }
            }
            cursor.close()

            return players
        }
    }
