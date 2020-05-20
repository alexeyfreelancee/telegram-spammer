package com.example.telegramspam

import com.example.telegramspam.utils.log
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun stopServiceByAction(){
        val phone = "+79175878779"
        val action = "com.example.telegramspam.services.STOP_PARSE@+79175878779"
        val result = action.split("@")[1]

        assertEquals(phone, result)
    }
}
