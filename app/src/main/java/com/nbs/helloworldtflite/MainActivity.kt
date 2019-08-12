package com.nbs.helloworldtflite

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private lateinit var tflite: Interpreter

    private var tfliteOptions: Interpreter.Options = Interpreter.Options()

    private lateinit var tfliteModel: MappedByteBuffer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initTfLite()
    }

    private fun initTfLite() {
        try {
            tfliteModel = loadModelFile()
            tfliteOptions.setNumThreads(1)
            tflite = Interpreter(tfliteModel, tfliteOptions)
        }catch (e: Exception){
            e.printStackTrace()
        }

        btnCalculate.setOnClickListener {
            val celcius = edtCelcius.text.toString().trim().toFloat()

            getFahrenheit(celcius)

            doInference(celcius)
        }
    }

    private fun doInference(celcius: Float){
        val input = floatArrayOf(celcius)
        val output = ByteBuffer.allocateDirect(4)
        output.order(ByteOrder.nativeOrder())
        tflite.run(input, output)
        output.rewind()
        val prediction = output.float
        tvResultML.text = "$prediction Fahrenheit\n(from ML)"
    }

    private fun getFahrenheit(celcius: Float) {
        val fahrenheit = (celcius * 9/5) + 32
        tvResultTraditional.text = "$fahrenheit Fahrenheit\n (from Traditional)"
    }

    private fun loadModelFile(): MappedByteBuffer{
        val fileDescriptor = this.assets.openFd("model_1000.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val starOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, starOffset, declaredLength)
    }
}
