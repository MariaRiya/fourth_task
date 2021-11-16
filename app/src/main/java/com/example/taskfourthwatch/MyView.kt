package com.example.taskfourthwatch

import android.content.Context
import android.graphics.*
import android.icu.util.Calendar
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlinx.coroutines.*
import kotlin.math.cos
import kotlin.math.sin


class MyView(internal var context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    var centerX = 0f
    var centerY = 0f
    var r = 0f
    var r2 = 0f
    var r3 = 0f
    var matrix1 = Matrix()
    val stroke_width_small = 10f
    val stroke_width_big = 50f
    var length_of_circle = 0f
    var point_of_end_second_hand = PointF()
    var point_of_end_minute_hand = PointF()
    var point_of_text = PointF()
    var first = true
    var angle = 6f
    var path_sec = Path()
    var path_min = Path()
    var path_hour = Path()
    val paint_main_circuit = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = stroke_width_small
    }
    val paint_minutes_circuit = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = stroke_width_small
    }
    val paint_hours_circuit = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = stroke_width_big
    }
    val paint_sec = Paint().apply {
        strokeWidth = 15f
        color = Color.RED
        style = Paint.Style.STROKE
    }
    val paint_min = Paint().apply {
        strokeWidth = 25f
        color = Color.BLACK
        style = Paint.Style.STROKE
        textSize = 15f
    }
    val paint_text = Paint().apply {
        strokeWidth = 5f
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
        textSize = 60f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        Log.d("TAG", "w = " + w + " h = " + h)
        if(w > h){
            r = h / 3f
        }else{
            r = w / 4f
        }
        r2 = r * 0.95f

        r3 = r * 0.75f

        val r_m = r * 0.85f

        val r_h = r * 0.60f

        point_of_end_second_hand = PointF(centerX, centerY - r)

        point_of_end_minute_hand = PointF(centerX, centerY - r_m)

        val point_of_end_hour_hand = PointF(centerX, centerY - r_h)

        point_of_text = PointF(centerX, centerY - r3)


        length_of_circle = (r2 * 2f * Math.PI).toFloat()
        val koef = 0.0035f
        val minutes = 60
        val hours = 12
        val dash_size_m = length_of_circle * koef
        val space_m = (length_of_circle - dash_size_m * minutes) / minutes
        val dash_size_h = length_of_circle * koef
        val space_h = (length_of_circle - dash_size_h * hours) / hours
        paint_main_circuit.strokeWidth = dash_size_h*2

        paint_minutes_circuit.setPathEffect(DashPathEffect(floatArrayOf(dash_size_m, space_m), 0F))
        paint_hours_circuit.setPathEffect(DashPathEffect(floatArrayOf(dash_size_h, space_h), 0F))

        path_sec.moveTo(centerX, centerY)
        path_sec.lineTo(point_of_end_second_hand.x, point_of_end_second_hand.y)
        path_sec.close()

        path_min.moveTo(centerX, centerY)
        path_min.lineTo(point_of_end_minute_hand.x, point_of_end_minute_hand.y)
        path_min.close()

        path_hour.moveTo(centerX, centerY)
        path_hour.lineTo(point_of_end_hour_hand.x, point_of_end_hour_hand.y)
        path_hour.close()


        GlobalScope.launch {

            while (true) {
                val now = Calendar.getInstance().time
                Log.d("TAG", "time: " + now.hours + ":" + now.minutes + ":" + now.seconds)

                matrix1.reset()
                if(first){
                    angle = 6f * now.seconds
                    matrix1.setRotate(angle, centerX, centerY)
                    path_sec.transform(matrix1)
                    matrix1.reset()
                    angle = 6f * now.minutes
                    matrix1.setRotate(angle, centerX, centerY)
                    path_min.transform(matrix1)
                    matrix1.reset()
                    angle = (now.hours + now.minutes / 60f)  * 30f
                    matrix1.setRotate(angle, centerX, centerY)
                    path_hour.transform(matrix1)
                    first = false

                }else {
                    matrix1.setRotate(6f, centerX, centerY)
                    path_sec.transform(matrix1)
                    if(now.seconds == 0){
                        path_min.transform(matrix1)
                    }
                    if(now.seconds == 0) {
                        matrix1.reset()
                        matrix1.setRotate(0.1f, centerX, centerY)
                        path_hour.transform(matrix1)
                    }
                }
                invalidate()
                delay(1000)

            }
        }.start()


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //контур часов, деления
        canvas.drawCircle(centerX, centerY, r, paint_main_circuit)
        canvas.drawCircle(centerX, centerY, r2, paint_minutes_circuit)
        canvas.drawCircle(centerX, centerY, r2, paint_hours_circuit)

        var angleInRadians = 0f
        val r = Rect()
        var j = 0

        //цифры на циферблате
        for(i in 12 downTo 1){

            j++
            val newX = point_of_text.x - centerX
            val newY = point_of_text.y - centerY

            val newX0 = newX * cos(angleInRadians) - newY * sin(angleInRadians) + centerX
            val newY0 = newX * sin(angleInRadians) + newY * cos(angleInRadians) + centerY



            val text = "" + i

            paint_text.getTextBounds(text, 0, text.length, r)

            canvas.drawText(text, newX0 - r.width() /2, newY0 + r.height() /2, paint_text)

            angleInRadians = -(( j * 30f) * Math.PI / 180).toFloat()

        }

        //стрелки
        canvas.drawPath(path_sec, paint_sec)
        canvas.drawPath(path_min, paint_min)
        canvas.drawPath(path_hour, paint_min)


    }


}