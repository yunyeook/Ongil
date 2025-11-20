package kr.co.ongil.domain.handler

import android.content.Context
import kr.co.ongil.domain.model.FcmMessage

interface SosActionHandler {
    fun startSosAction(context: Context, message: FcmMessage)
    fun stopSosAction()
}