package kr.co.ongil.domain.usecase.fcm

import android.content.Context
import kr.co.ongil.domain.handler.SosActionHandler
import kr.co.ongil.domain.model.FcmMessage
import javax.inject.Inject

class HandleSosUseCase @Inject constructor(
    private val sosActionHandler: SosActionHandler
) {
    suspend operator fun invoke(context: Context, message: FcmMessage) {
        sosActionHandler.startSosAction(context, message)
    }
}