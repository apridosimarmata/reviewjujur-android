package id.sireto.reviewjujur.main.reviews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.sireto.reviewjujur.R
import id.sireto.reviewjujur.databinding.FragmentReviewCreatedBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ReviewCreatedFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentReviewCreatedBinding

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
        binding = FragmentReviewCreatedBinding.inflate(layoutInflater)
        return binding.root
    }
}