package com.example.portableDataTerminal.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.portableDataTerminal.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InfoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SetTextI18n")
    fun set_product_name(name: String) {
        val r = view?.findViewById<EditText>(R.id.editText_product_name)
        if (r != null) {
            r.setText(name)
        }
    }

    @SuppressLint("SetTextI18n")
    fun set_product_description(description: String) {
        val r = view?.findViewById<EditText>(R.id.editText_product_description)
        if (r != null) {
            r.setText(description)
        }
    }

    @SuppressLint("SetTextI18n")
    fun set_product_article(article: String) {
        val r = view?.findViewById<EditText>(R.id.editText_product_article)
        if (r != null) {
            r.setText(article)
        }
    }

    @SuppressLint("SetTextI18n")
    fun set_product_barcode(barcode: String) {
        val r = view?.findViewById<EditText>(R.id.editText_product_barcode)
        if (r != null) {
            r.setText(barcode)
        }
    }

    @SuppressLint("SetTextI18n")
    fun set_product_count(count: String) {
        val r = view?.findViewById<EditText>(R.id.editText_product_count)
        if (r != null) {
            r.setText(count)
        }
    }

    fun get_view(): View? {
        return view
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}