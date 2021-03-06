package com.example.rviciana.safeflight.view.form

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import com.example.rviciana.safeflight.R
import com.example.rviciana.safeflight.SafeFlightsApplication
import com.example.rviciana.safeflight.domain.bo.Airport
import com.example.rviciana.safeflight.domain.bo.AirportsResponse
import com.example.rviciana.safeflight.view.NavigatorImpl
import com.example.rviciana.safeflight.view.RootActivity
import com.example.rviciana.safeflight.view.form.di.FormModule
import kotlinx.android.synthetic.main.activity_form.*
import javax.inject.Inject

class FormActivity : RootActivity(), FormContract.View {

    @Inject
    lateinit var presenter: FormContract.Presenter

    private val Activity.app: SafeFlightsApplication get() = application as SafeFlightsApplication
    private val component by lazy { app.component.plus(FormModule()) }

    companion object {

        fun getCallingIntent(context: Context, airportsResponse: AirportsResponse): Intent {
            val intent = Intent(context, FormActivity::class.java)
            intent.putExtra(ARG_AIRPORTS, airportsResponse)
            return intent
        }

        private const val ARG_AIRPORTS = "ARG_AIRPORTS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getExtras()
        setListeners()
    }

    private fun getExtras() {
        val extras = intent.extras
        extras?.apply {
            val airportsResponse = getParcelable(ARG_AIRPORTS) as AirportsResponse
            val airports = airportsResponse.airportResource.airports.airport

            val arrayAdapter = SuggestionsArrayAdapter(
                    this@FormActivity,
                    R.layout.suggestion_adapter_item,
                    ArrayList(airports),
                    ArrayList(airports)
            )

            searchViewOrigin.setAdapter(arrayAdapter)
            searchViewDestination.setAdapter(arrayAdapter)
        }
    }

    private fun setListeners() {
        var originAirport: Airport? = null
        var destinationAirport: Airport? = null

        with(searchViewOrigin) {
            onItemClickListener =
                    AdapterView.OnItemClickListener { parent, _, position, _ ->
                        originAirport = parent.adapter.getItem(position) as Airport
                    }
            setOnTouchListener(View.OnTouchListener { _, event ->
                if (event != null) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (event.rawX >= (right - compoundDrawables[2].bounds.width())) {
                            presenter.onOriginEraserClicked()
                            return@OnTouchListener true
                        }
                    }
                }
                return@OnTouchListener false
            })
        }

        with(searchViewDestination) {
            onItemClickListener =
                    AdapterView.OnItemClickListener { parent, _, position, _ ->
                        destinationAirport = parent.adapter.getItem(position) as Airport
                    }

            setOnTouchListener(View.OnTouchListener { _, event ->
                if (event != null) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (event.rawX >= (right - compoundDrawables[2].bounds.width())) {
                            presenter.onDestinationEraserClicked()
                            return@OnTouchListener true
                        }
                    }
                }
                return@OnTouchListener false
            })
        }

        searchFlights.setOnClickListener {
            val searchViewOriginText = searchViewOrigin.text
            val searchViewDestinationText = searchViewDestination.text
            if (originAirport != null && destinationAirport != null
                    && searchViewOriginText.isNotEmpty()
                    && searchViewDestinationText.isNotEmpty()) {
                presenter.onAirportsReady(originAirport!!, destinationAirport!!)
            } else {
                if (searchViewOriginText.isEmpty())
                    presenter.onOriginInputError()

                if (searchViewDestinationText.isEmpty())
                    presenter.onDestinationInputError()
            }
        }
    }

    override fun initializeInjector() {
        component.inject(this@FormActivity)
    }

    override fun initializePresenter() {
        presenter.setView(this@FormActivity)
    }

    override fun getLayoutResourceId(): Int = R.layout.activity_form

    override fun navigateToFlightsListActivity(originAirport: Airport, destinationAirport: Airport) {
        NavigatorImpl(this@FormActivity)
                .navigateToFlightsListActivity(originAirport, destinationAirport)
    }

    override fun showOriginInputError() {
        searchViewOriginLayout.error = getString(R.string.missing_test_error)
    }

    override fun showDestinationInputError() {
        searchViewDestinationLayout.error = getString(R.string.missing_test_error)
    }

    override fun hideOriginInputError() {
        searchViewOriginLayout.isErrorEnabled = false
    }

    override fun hideDestinationInputError() {
        searchViewDestinationLayout.isErrorEnabled = false
    }

    override fun clearSearchViewOrigin() {
        searchViewOrigin.text.clear()
    }

    override fun clearSearchViewDestination() {
        searchViewDestination.text.clear()
    }

}
