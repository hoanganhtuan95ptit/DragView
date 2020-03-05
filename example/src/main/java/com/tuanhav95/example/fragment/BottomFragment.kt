package com.tuanhav95.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tuanhav95.drag.utils.inflate
import com.tuanhav95.example.R
import kotlinx.android.synthetic.main.fragment_bottom.*
import kotlinx.android.synthetic.main.item_normal.view.*

class BottomFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView.adapter = ListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    }


    class ListAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent.inflate(R.layout.item_normal))
        }

        override fun getItemCount(): Int {
            return 20
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.ivPhoto)
                    .load(R.drawable.photo)
                    .into(holder.itemView.ivPhoto)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}