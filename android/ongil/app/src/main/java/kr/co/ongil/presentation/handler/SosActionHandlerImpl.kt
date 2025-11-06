package kr.co.ongil.presentation.handler

import android.content.Context
import android.util.Log
import kr.co.ongil.domain.handler.SosActionHandler
import kr.co.ongil.domain.model.FcmMessage
import javax.inject.Inject

class SosActionHandlerImpl @Inject constructor() : SosActionHandler {

    override fun startSosAction(context: Context, message: FcmMessage) {
        Log.d("SosActionHandlerImpl", "startHelpAction")
    }

}