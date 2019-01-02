package com.example.rviciana.safeflight.domain.usecase

import com.example.rviciana.safeflight.data.flights.FlightsRepository
import com.example.rviciana.safeflight.domain.bo.ScheduleResource
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import javax.inject.Inject

class FlightsUseCase @Inject constructor(private val flightsRepository: FlightsRepository,
                                         private val subscribeOn: Scheduler,
                                         private val observeOn: Scheduler
) {

    private var disposable: Disposable = Disposables.empty()

    fun execute(accessToken: String, origin: String, destination: String, fromDate: String,
                onComplete: (ScheduleResource) -> Unit, onError: (Throwable) -> Unit) {
        disposable = flightsRepository.getFlights(accessToken, origin, destination, fromDate)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(onComplete, onError)
    }

    fun dispose() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
    }
}