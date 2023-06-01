package com.example.portableDataTerminal.Fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.portableDataTerminal.R

/*
 * Класс, содержащий в себе обработку информационного фрагмента для
 * других страниц программы
 */
class InfoFragment : Fragment() {

    /*
     * Обработчик создания представления фрагмента
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }
}