package com.pos.consults.utils

import java.text.SimpleDateFormat
import java.util.*

class Utils {
    companion object {
        fun parseDatetime(date: String): Date = SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
    }
}