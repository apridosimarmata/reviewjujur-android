package id.sireto.reviewjujur.main.reviews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.sireto.reviewjujur.R
import id.sireto.reviewjujur.databinding.FragmentReviewsBinding
import id.sireto.reviewjujur.models.BaseResponse
import id.sireto.reviewjujur.models.ReviewResponse
import id.sireto.reviewjujur.services.api.ApiService
import retrofit2.Retrofit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ReviewsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentReviewsBinding
    private lateinit var apiService: ApiService
    private lateinit var retrofit: Retrofit
    private var response = BaseResponse()
    private var reviews = arrayListOf<ReviewResponse>()
    private lateinit var reviewsRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewsBinding.inflate(layoutInflater)
        setupUserReviewsRecyclerView()
        return binding.root
    }

    private fun setupUserReviewsRecyclerView(){

    }
}