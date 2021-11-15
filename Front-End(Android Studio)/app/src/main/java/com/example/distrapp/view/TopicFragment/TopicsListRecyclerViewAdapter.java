package com.example.distrapp.view.TopicFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.distrapp.R;

import com.example.distrapp.view.TopicFragment.TopicsListFragment.OnListFragmentInteractionListener;
import java.util.ArrayList;

public class TopicsListRecyclerViewAdapter extends RecyclerView.Adapter<TopicsListRecyclerViewAdapter.ViewHolder> implements Filterable {
    private final ArrayList<String> mValues;
    private ArrayList<String> mValuesFull;
    private final OnListFragmentInteractionListener mListener;


    public TopicsListRecyclerViewAdapter(ArrayList<String> items,OnListFragmentInteractionListener listener){
        mValues =items;
        mListener =listener;
        mValuesFull = new ArrayList<>(mValues);
    }

    /**
     *
     * @param parent the view parent
     * @param viewType the view type
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_topics_list_item, parent, false);
        return new ViewHolder(view);
    }


    /**
     *
     * @param holder the holder
     * @param position the index of the item
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String currentTopic = mValues.get(position);
        holder.mItem = currentTopic;
        holder.txttopicTitle.setText(currentTopic);

        holder.btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    /**
     * get the number of tournaments in the list
     * @return the number of tournaments
     */
    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView txttopicTitle;
        public final ImageButton btnSubscribe;
        public String mItem;

        /**
         *  constructor
         * @param view the view
         */
        public ViewHolder(View view) {
            super(view);
            mView = view;
            txttopicTitle = view.findViewById(R.id.txt_topic_name);
            btnSubscribe = view.findViewById(R.id.btn_subscribe);
        }

        /**
         * represents the basic info of the view holder as a string
         * @return the string representation of the view holder contents
         */
        @Override
        public String toString() {
            return super.toString() + " '" + txttopicTitle.getText() + "'";
        }
    }


    @Override
    public Filter getFilter(){
        return exampleFilter;

    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<String> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(mValuesFull);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (String string : mValuesFull){
                    if (string.contains(filterPattern)){
                        filteredList.add(string);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mValues.clear();
            mValues.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };

}
