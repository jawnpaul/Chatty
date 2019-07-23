package ng.org.knowit.chatty.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;

import ng.org.knowit.chatty.R;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    Context mContext;
    ArrayList<String> mStringList;
    private final OnListItemClickListener mOnListItemClickListener;

    public SuggestionAdapter(Context context, ArrayList<String> stringList, OnListItemClickListener onListItemClickListener){
        this.mContext = context;
        this.mStringList = stringList;
        this.mOnListItemClickListener = onListItemClickListener;
    }

    public interface OnListItemClickListener {
        void onListItemClick(int position);
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {

        holder.suggestionChip.setText(mStringList.get(position));


    }

    @Override
    public int getItemCount() {
        return mStringList.size();
    }

    class SuggestionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final Chip suggestionChip;

        public SuggestionViewHolder(View view){
            super(view);

            suggestionChip = view.findViewById(R.id.suggestionChip);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d("Suggestion adapter", String.valueOf(position));
            mOnListItemClickListener.onListItemClick(position);
        }
    }
}
