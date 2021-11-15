package com.example.distrapp.view.VideoFragment;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.distrapp.R;
import com.example.distrapp.phase1Code.Value;
import com.example.distrapp.view.LoggedInUser;
import com.example.distrapp.view.VideoFragment.VideosListFragment.OnListFragmentInteractionListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VideosListRecyclerViewAdapter extends RecyclerView.Adapter<VideosListRecyclerViewAdapter.ViewHolder> implements Filterable {

    private final ArrayList<Value> mValues;
    private ArrayList<Value> mValuesFull;
    private final OnListFragmentInteractionListener mListener;



    public VideosListRecyclerViewAdapter(ArrayList<Value> items, OnListFragmentInteractionListener listener){
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
    public @NotNull ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_videos_list_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     *
     * @param holder the holder
     * @param position the index of the item
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Value currentVideo = mValues.get(position);

        if (LoggedInUser.isEmulator()){
            Uri uri = Uri.parse(VideosListFragment.path+currentVideo.getVideoName());
            MediaMetadataRetriever mMMR = new MediaMetadataRetriever();


            mMMR.setDataSource(String.valueOf(uri));

            Bitmap bitmap = mMMR.getFrameAtTime();
            ImageButton imageButton = holder.btnSelect;
            imageButton.setImageBitmap(bitmap);
        }

        holder.mItem = currentVideo;
        holder.txtvideoTitle.setText(currentVideo.getVideoName());
        holder.btnSelect.setOnClickListener(new View.OnClickListener() {
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
        public final TextView txtvideoTitle;
        public final ImageButton btnSelect;
        public Value mItem;

        /**
         *  constructor
         * @param view the view
         */
        public ViewHolder(View view) {
            super(view);
            mView = view;
            txtvideoTitle = view.findViewById(R.id.txt_video_name);
            btnSelect = view.findViewById(R.id.btn_select_video);
        }

        /**
         * represents the basic info of the view holder as a string
         * @return the string representation of the view holder contents
         */
        @Override
        public String toString() {
            return super.toString() + " '" + txtvideoTitle.getText() + "'";
        }
    }

    @Override
    public Filter getFilter(){
        return exampleFilter;

    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Value> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(mValuesFull);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Value value : mValuesFull){
                    if (value.getVideoName().contains(filterPattern)){
                        filteredList.add(value);
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
