package id.sireto.reviewjujur.main.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.internal.LinkedTreeMap
import id.sireto.reviewjujur.databinding.FragmentHomeBinding
import id.sireto.reviewjujur.main.ChooseLocationActivity
import id.sireto.reviewjujur.main.SearchActivity
import id.sireto.reviewjujur.models.*
import id.sireto.reviewjujur.rv.adapters.BusinessMainCardAdapter
import id.sireto.reviewjujur.rv.decorators.HorizontalDecorator
import id.sireto.reviewjujur.services.api.ApiClient
import id.sireto.reviewjujur.services.api.ApiService
import id.sireto.reviewjujur.utils.Constants
import id.sireto.reviewjujur.utils.Converter
import id.sireto.reviewjujur.utils.SharedPref
import id.sireto.reviewjujur.utils.UI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private lateinit var apiService: ApiService
    private lateinit var retrofit: Retrofit
    private var response = BaseResponse()

    private lateinit var rvMostReviewedBusiness : RecyclerView
    private lateinit var rvBestScoringBusinesses : RecyclerView
    private lateinit var mostReviewedRvAdapter: BusinessMainCardAdapter
    private lateinit var bestScoringRvAdapter: BusinessMainCardAdapter
    private val mostReviewedBusinesses = arrayListOf<BusinessResponse>()
    private val bestScoringBusinesses = arrayListOf<BusinessResponse>()

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentHomeBinding

    private var previousSelectedLocationUid : String? = null
    private var loadedBefore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        retrofit = ApiClient.getApiClient()
        apiService = retrofit.create(ApiService::class.java)
        previousSelectedLocationUid = SharedPref.getStringFromSharedPref(Constants.KEY_SELECTED_LOCATION)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        setupListeners()
        rvMostReviewedBusiness = binding.rvMostReview
        rvMostReviewedBusiness.addItemDecoration(HorizontalDecorator(20))
        mostReviewedRvAdapter = BusinessMainCardAdapter(requireActivity())
        rvBestScoringBusinesses = binding.rvBestScore
        rvBestScoringBusinesses.addItemDecoration(HorizontalDecorator(20))
        bestScoringRvAdapter = BusinessMainCardAdapter(requireActivity())
        return binding.root
    }

    private fun setupListeners(){
        binding.homeChooseLocationBtn.setOnClickListener{
            startActivity(Intent(requireActivity(), ChooseLocationActivity::class.java))
        }

        /*binding.homeFragmentSearch.onFocusChangeListener = (View.OnFocusChangeListener { p0, p1 ->
            if(p1){
                startActivity(Intent(requireContext(), SearchActivity::class.java))
            }
        })*/

        binding.homeFragmentSearch.setOnClickListener{
            startActivity(Intent(requireContext(), SearchActivity::class.java)
                .putExtra("locationUid", SharedPref.getStringFromSharedPref(Constants.KEY_SELECTED_LOCATION)))
        }
    }

    override fun onResume() {
        super.onResume()
        with(SharedPref.getStringFromSharedPref(Constants.KEY_SELECTED_LOCATION)){
            if (this != null){
                binding.homeLocationNotChoosen.visibility = View.INVISIBLE
                showDetails()

                if(!loadedBefore){
                    setupBusinesses(this)
                }

                if (this != previousSelectedLocationUid){

                    setupBusinesses(this)
                    previousSelectedLocationUid = this
                }
            }else{
                binding.homeLocationNotChoosen.visibility = View.VISIBLE
                hideDetails()
            }
        }
    }

    private fun setupBusinesses(locationUid: String){
        loadedBefore = true

        val businessPagination =
            BusinessPagination(
                locationUid = locationUid,
                limit = null,
                page = null,
                rows = arrayListOf(),
                sort = null,
                search = null,
                province = "",
                location = ""
            )

        setupMostReviewBusinesses(businessPagination)
        setupBestScoringBusinesses(businessPagination)
    }

    @SuppressLint("SetTextI18n")
    private fun setupBestScoringBusinesses(businessPagination: BusinessPagination){
        bestScoringBusinesses.clear()

        lifecycleScope.launch(Dispatchers.Main){

            val getBestScoringBusinesses = lifecycleScope.async {
                response = try {
                    apiService.searchBusiness(
                        businessPagination.limit,
                        businessPagination.page,
                        businessPagination.locationUid,
                        businessPagination.search,
                        Constants.KEY_BEST_SCORE
                    ).body()!!
                }catch (e: Exception){
                    BaseResponse(Meta(code = 0, message = "Error : ${e.cause}"), null)
                }
            }

            getBestScoringBusinesses.await()
            if (response.meta.code == 200){
                Converter.anyToBusinessPagination(response.result as LinkedTreeMap<String, Any>).let { pagination ->
                    pagination.rows.map {
                        bestScoringBusinesses.add(it)
                    }
                    binding.homeLocationName.text = pagination.location + ","
                    binding.homeProvinceName.text = pagination.province
                }
                showBestScoringBusinesses()
            }else{
                UI.showSnackbarByResponseCode(response.meta, binding.homeChooseLocationBtn)
            }
        }
    }

    private fun showBestScoringBusinesses(){
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        bestScoringRvAdapter.businesses.clear()
        bestScoringRvAdapter.notifyDataSetChanged()
        rvBestScoringBusinesses.layoutManager = layoutManager
        rvBestScoringBusinesses.adapter = bestScoringRvAdapter
        bestScoringRvAdapter.businesses.addAll(bestScoringBusinesses)
        bestScoringRvAdapter.notifyDataSetChanged()

        rvBestScoringBusinesses.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

            override fun onTouchEvent(view: RecyclerView, event: MotionEvent) {}

            override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_SCROLL -> {
                        rvBestScoringBusinesses.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setupMostReviewBusinesses(businessPagination : BusinessPagination){
        mostReviewedBusinesses.clear()

        lifecycleScope.launch(Dispatchers.Main){

            val getMostReviewedBusinesses = lifecycleScope.async {
                response = try {
                    apiService.searchBusiness(
                        businessPagination.limit,
                        businessPagination.page,
                        businessPagination.locationUid,
                        businessPagination.search,
                        Constants.KEY_MOST_REVIEW
                    ).body()!!
                }catch (e: Exception){
                    BaseResponse(Meta(code = 0, message = "Error : ${e.cause}"), null)
                }
            }

            getMostReviewedBusinesses.await()
            if (response.meta.code == 200){
                Converter.anyToBusinessPagination(response.result as LinkedTreeMap<String, Any>).let {
                    it.rows.map {
                        mostReviewedBusinesses.add(it)
                    }
                    binding.homeLocationName.text = it.location + ","
                    binding.homeProvinceName.text = it.province
                }
                showMostReviewedBusiness()
            }else{
                UI.showSnackbarByResponseCode(response.meta, binding.homeChooseLocationBtn)
            }
        }
    }

    private fun showMostReviewedBusiness(){
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mostReviewedRvAdapter.businesses.clear()
        mostReviewedRvAdapter.notifyDataSetChanged()
        rvMostReviewedBusiness.layoutManager = layoutManager
        rvMostReviewedBusiness.adapter = mostReviewedRvAdapter
        mostReviewedRvAdapter.businesses.addAll(mostReviewedBusinesses)
        mostReviewedRvAdapter.notifyDataSetChanged()

        rvMostReviewedBusiness.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

            override fun onTouchEvent(view: RecyclerView, event: MotionEvent) {}

            override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_SCROLL -> {
                        rvMostReviewedBusiness.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun hideDetails(){
        val invisible = View.INVISIBLE
        with(binding){
            homeLocationName.visibility = invisible
            homeProvinceName.visibility = invisible
            homeBestScore.visibility = invisible
            homeMostReview.visibility = invisible
            rvBestScore.visibility = invisible
            rvMostReview.visibility = invisible
            homeSeeMoreBestScore.visibility = invisible
            homeSeeMoreMostReview.visibility = invisible
        }
    }

    private fun showDetails(){
        val visible = View.VISIBLE
        with(binding){
            homeLocationName.visibility = visible
            homeProvinceName.visibility = visible
            homeBestScore.visibility = visible
            homeMostReview.visibility = visible
            rvBestScore.visibility = visible
            rvMostReview.visibility = visible
            homeSeeMoreBestScore.visibility = visible
            homeSeeMoreMostReview.visibility = visible
        }
    }
}